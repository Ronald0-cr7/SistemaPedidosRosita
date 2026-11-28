package Controlador;

import Modelo.Pago;
import Modelo.PagoDAO;
import servicio.MercadoPagoService;

import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@WebServlet(
    name = "SVWebhookPago",
    urlPatterns = {"/SVWebhookPago"}
)
public class SVWebhookPago extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        try {

            // OBTENER DATOS DE LA NOTIFICACIÓN
            String mpOrderId =
                    request.getParameter("data.id");

            String tipo =
                    request.getParameter("type");

            String firma =
                    request.getHeader("x-signature");

            String requestId =
                    request.getHeader("x-request-id");

            String secreto =
                    System.getenv("MP_WEBHOOK_SECRET");

            // VALIDAR CONFIGURACIÓN
            if (secreto == null || secreto.isBlank()) {

                response.sendError(503);
                return;
            }

            if (mpOrderId == null
                    || firma == null
                    || requestId == null) {

                response.sendError(400);
                return;
            }

            // VALIDAR FIRMA DEL WEBHOOK
            if (!validarFirma(
                    firma,
                    requestId,
                    mpOrderId,
                    secreto)) {

                response.sendError(401);
                return;
            }

            // PROCESAR SOLAMENTE ÓRDENES
            if (!"order".equals(tipo)) {

                response.setStatus(200);
                return;
            }

            MercadoPagoService servicio =
                    new MercadoPagoService();

            PagoDAO pagoDAO = new PagoDAO();

            // CONSULTAR ESTADO REAL EN MERCADO PAGO
            JsonObject orden =
                    servicio.consultarOrden(mpOrderId);

            // OBTENER REFERENCIA INTERNA
            if (!orden.has("external_reference")
                    || orden.get("external_reference").isJsonNull()) {

                response.setStatus(200);
                return;
            }

            String referencia =
                    orden.get("external_reference")
                            .getAsString();

            try {

                UUID.fromString(referencia);

            } catch (IllegalArgumentException e) {

                response.setStatus(200);
                return;
            }

            // BUSCAR PAGO EN SUPABASE
            Pago pago =
                    pagoDAO.buscarPorId(referencia);

            if (pago == null) {

                response.setStatus(200);
                return;
            }

            if (pago.getMpOrderId() == null) {

                // Esperar que termine la creación de la orden
                response.sendError(503);
                return;
            }

            // VALIDAR IDENTIFICADORES
            if (!mpOrderId.equals(pago.getMpOrderId())

                    || !mpOrderId.equals(
                    orden.get("id").getAsString())) {

                response.sendError(409);
                return;
            }

            // VALIDAR MONEDA E IMPORTE
            if (!"PEN".equals(pago.getMoneda())

                    || !"PEN".equals(
                    orden.get("currency").getAsString())

                    || pago.getMonto().compareTo(
                    orden.get("total_amount")
                            .getAsBigDecimal()) != 0) {

                response.sendError(409);
                return;
            }

            // OBTENER ESTADO DE MERCADO PAGO
            String mpStatus =
                    orden.get("status").getAsString();

            String mpStatusDetail =
                    orden.get("status_detail").getAsString();

            String estado;

            // CONVERTIR ESTADO DE MERCADO PAGO
            switch (mpStatus) {

                case "processed":

                    boolean montoCompleto =
                            pago.getMonto().compareTo(
                            orden.get("total_paid_amount")
                                    .getAsBigDecimal()) == 0;

                    estado = "accredited".equals(mpStatusDetail)
                            && montoCompleto
                            ? "Aprobado" : "Pendiente";

                    break;

                case "failed":

                    estado = "Rechazado";
                    break;

                case "canceled":

                    estado = "Cancelado";
                    break;

                case "refunded":

                    estado = "Reembolsado";
                    break;

                case "created":
                case "processing":
                case "action_required":

                    estado = "Pendiente";
                    break;

                default:

                    response.sendError(503);
                    return;
            }

            // OBTENER FECHA DE ACTUALIZACIÓN
            OffsetDateTime actualizadoMP =
                    OffsetDateTime.parse(
                            orden.get("last_updated_date")
                                    .getAsString()
                    );

            // ACTUALIZAR ESTADO EN SUPABASE
            pagoDAO.actualizarEstadoVerificado(
                    pago.getId(),
                    mpOrderId,
                    estado,
                    mpStatus,
                    mpStatusDetail,
                    actualizadoMP
            );

            // CONFIRMAR RECEPCIÓN DEL WEBHOOK
            response.setStatus(200);

        } catch (Exception e) {

            log("Error al procesar notificación de pago", e);

            response.sendError(503);
        }
    }

    // VALIDAR FIRMA DE MERCADO PAGO
    private boolean validarFirma(
            String firma,
            String requestId,
            String dataId,
            String secreto) throws Exception {

        if (firma == null || requestId == null
                || dataId == null || secreto == null) {

            return false;
        }

        String ts = null;
        String v1 = null;

        // EXTRAER TIMESTAMP Y FIRMA
        for (String parte : firma.split(",")) {

            String[] valores =
                    parte.trim().split("=", 2);

            if (valores.length == 2) {

                String clave = valores[0].trim();
                String valor = valores[1].trim();

                if ("ts".equals(clave)) {

                    if (ts != null) {
                        return false;
                    }

                    ts = valor;
                }

                if ("v1".equals(clave)) {

                    if (v1 != null) {
                        return false;
                    }

                    v1 = valor;
                }
            }
        }

        // VALIDAR FORMATO
        if (ts == null || v1 == null
                || !ts.matches("[0-9]+")
                || !v1.matches("[a-fA-F0-9]{64}")) {

            return false;
        }

        // CONSTRUIR MENSAJE PARA VALIDACIÓN
        String manifest =
                "id:" + dataId.toLowerCase(Locale.ROOT)
                + ";request-id:" + requestId
                + ";ts:" + ts + ";";

        // GENERAR FIRMA HMAC SHA256
        Mac mac = Mac.getInstance("HmacSHA256");

        SecretKeySpec claveSecreta =
                new SecretKeySpec(
                        secreto.getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                );

        mac.init(claveSecreta);

        byte[] firmaCalculada =
                mac.doFinal(
                        manifest.getBytes(StandardCharsets.UTF_8)
                );

        byte[] firmaRecibida =
                HexFormat.of().parseHex(v1);

        // COMPARAR FIRMAS DE FORMA SEGURA
        return MessageDigest.isEqual(
                firmaCalculada,
                firmaRecibida
        );
    }
}