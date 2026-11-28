package Controlador;

import Modelo.Cliente;
import Modelo.Pago;
import Modelo.PagoDAO;
import Modelo.PedidoDAO;
import servicio.MercadoPagoService;

import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

@WebServlet(
    name = "SVResultadoPago",
    urlPatterns = {"/ResultadoPago"}
)
public class SVResultadoPago extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(
                    request.getContextPath() + "/IndexSistema");
            return;
        }

        try {

            String pagoId = request.getParameter("pago");

            if (pagoId == null) {
                response.sendError(400);
                return;
            }

            UUID.fromString(pagoId);

            PagoDAO pagoDAO = new PagoDAO();
            Pago pago = pagoDAO.buscarPorId(pagoId);

            if (pago == null) {
                response.sendError(404);
                return;
            }

            // VALIDAR PROPIEDAD DEL PEDIDO
            Cliente cliente = (Cliente)
                    session.getAttribute("clienteLogueado");

            String pedidoSesion = (String)
                    session.getAttribute("pedidoRegistrado");

            boolean autorizado =
                    pago.getPedidoId().equals(pedidoSesion);

            if (!autorizado && cliente != null) {

                autorizado = new PedidoDAO().buscarPorCliente(
                        pago.getPedidoId(),
                        cliente.getId()) != null;
            }

            if (!autorizado) {
                response.sendError(403);
                return;
            }

            String estado = "Pendiente";

            // CONSULTAR ESTADO REAL EN MERCADO PAGO
            if (pago.getMpOrderId() != null) {

                MercadoPagoService servicio =
                        new MercadoPagoService();

                JsonObject orden = servicio.consultarOrden(
                        pago.getMpOrderId());

                // VALIDAR REFERENCIA, ORDEN, MONEDA E IMPORTE
                if (!pago.getId().equals(
                        orden.get("external_reference").getAsString())

                        || !pago.getMpOrderId().equals(
                        orden.get("id").getAsString())

                        || !"PEN".equals(
                        orden.get("currency").getAsString())

                        || pago.getMonto().compareTo(
                        orden.get("total_amount")
                                .getAsBigDecimal()) != 0) {

                    response.sendError(409);
                    return;
                }

                String status =
                        orden.get("status").getAsString();

                String detalle =
                        orden.get("status_detail").getAsString();

                if ("processed".equals(status)
                        && "accredited".equals(detalle)
                        && pago.getMonto().compareTo(
                        orden.get("total_paid_amount")
                                .getAsBigDecimal()) == 0) {

                    estado = "Aprobado";

                } else if ("failed".equals(status)) {

                    estado = "Rechazado";

                } else if ("canceled".equals(status)) {

                    estado = "Cancelado";

                } else if ("refunded".equals(status)) {

                    estado = "Reembolsado";
                }
            }

            request.setAttribute("pago", pago);
            request.setAttribute("estadoPago", estado);

            request.getRequestDispatcher(
                    "/WEB-INF/vistas/tienda/resultadoPago.jsp"
            ).forward(request, response);

        } catch (IllegalArgumentException e) {

            response.sendError(400);

        } catch (Exception e) {

            log("Error al consultar resultado del pago", e);

            response.sendError(
                    503, "No se pudo verificar el pago");
        }
    }
}