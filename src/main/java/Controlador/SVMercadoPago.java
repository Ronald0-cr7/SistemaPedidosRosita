package Controlador;

import Modelo.Pago;
import Modelo.PagoDAO;
import servicio.MercadoPagoService;

import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URI;

@WebServlet(
    name = "SVMercadoPago",
    urlPatterns = {"/SVMercadoPago"}
)
public class SVMercadoPago extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendError(403);
            return;
        }

        // VALIDAR SOLICITUD
        if (!Boolean.TRUE.equals(
                request.getAttribute("inicioPagoValidado"))) {

            response.sendError(403);
            return;
        }

        String pedidoId = (String)
                session.getAttribute("pedidoRegistrado");

        if (pedidoId == null) {

            response.sendError(
                    400, "No existe un pedido registrado");
            return;
        }

        // OBTENER CORREO DEL COMPRADOR
        String correo = request.getParameter("correo_pago");

        if (correo == null || !correo.trim().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            response.sendError(
                    400, "Correo del comprador inválido");
            return;
        }

        correo = correo.trim();

        try {

            PagoDAO pagoDAO = new PagoDAO();

            MercadoPagoService servicio =
                    new MercadoPagoService();

            Pago pago = pagoDAO.buscarPorPedido(pedidoId);

            if (pago == null) {

                response.sendError(
                        404, "Pago no encontrado");
                return;
            }

            if (!"Pendiente".equals(pago.getEstado())) {

                response.sendError(
                        409, "El pago no está disponible");
                return;
            }

            // COMPROBAR ORDEN EXISTENTE
            if (pago.getMpOrderId() != null) {

                JsonObject ordenExistente =
                        servicio.consultarOrden(
                                pago.getMpOrderId());

                String estado =
                        ordenExistente.get("status")
                                .getAsString();

                if (!"created".equals(estado)) {

                    response.sendError(
                            409, "El pago requiere verificación");

                    return;
                }

                if (urlValida(pago.getCheckoutUrl())) {

                    response.sendRedirect(
                            pago.getCheckoutUrl());

                    return;
                }

                response.sendError(503);

                return;
            }

            // CREAR ORDEN CON CORREO DEL CLIENTE
            JsonObject orden =
                    servicio.crearOrden(pago, correo);

            String[] campos = {
                "id",
                "checkout_url",
                "status",
                "external_reference",
                "total_amount"
            };

            // VALIDAR RESPUESTA DE MERCADO PAGO
            for (String campo : campos) {

                if (!orden.has(campo)
                        || orden.get(campo).isJsonNull()) {

                    log("Campo faltante: " + campo);

                    response.sendError(
                            502, "Respuesta incompleta");

                    return;
                }
            }

            String mpOrderId =
                    orden.get("id").getAsString();

            String checkoutUrl =
                    orden.get("checkout_url").getAsString();

            String mpStatus =
                    orden.get("status").getAsString();

            String mpStatusDetail =
                    orden.has("status_detail")
                    && !orden.get("status_detail").isJsonNull()
                    ? orden.get("status_detail").getAsString()
                    : null;

            // VALIDAR REFERENCIA E IMPORTE
            if (!pago.getId().equals(
                    orden.get("external_reference").getAsString())

                    || pago.getMonto().compareTo(
                    orden.get("total_amount").getAsBigDecimal()) != 0

                    || !urlValida(checkoutUrl)) {

                response.sendError(
                        502, "Orden de pago inválida");

                return;
            }

            // GUARDAR ORDEN EN SUPABASE
            boolean actualizado =
                    pagoDAO.actualizarOrdenMP(
                            pago.getId(),
                            mpOrderId,
                            checkoutUrl,
                            mpStatus,
                            mpStatusDetail,
                            null
                    );

            if (!actualizado) {

                response.sendError(
                        500, "No se pudo guardar el pago");
                return;
            }

            // REDIRIGIR A MERCADO PAGO
            response.sendRedirect(checkoutUrl);

        } catch (Exception e) {

            log("Error al iniciar Mercado Pago", e);

            response.sendError(
                    503, "No se pudo iniciar el pago");
        }
    }

    private boolean urlValida(String url) {

        try {

            URI uri = URI.create(url);

            return "https".equalsIgnoreCase(uri.getScheme())
                    && "www.mercadopago.com.pe"
                            .equalsIgnoreCase(uri.getHost())
                    && uri.getUserInfo() == null
                    && (uri.getPort() == -1
                        || uri.getPort() == 443);

        } catch (Exception e) {

            return false;
        }
    }
}