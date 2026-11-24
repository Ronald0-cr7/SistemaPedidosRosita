<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Modelo.Pedido"%>
<%@page import="Modelo.PedidoDetalle"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>

<%!
    private String esc(String valor) {

        if (valor == null) return "";

        return valor.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    String ctx = request.getContextPath();

    Pedido pedido = (Pedido) request.getAttribute("pedido");

    List<PedidoDetalle> detalles =
        (List<PedidoDetalle>) request.getAttribute("detalles");

    if (pedido == null || detalles == null) {

        response.sendRedirect(ctx + "/HistorialPedidos");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Detalle de mi pedido | Minimarket Rosita</title>

    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">

    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <link rel="stylesheet"
          href="<%=ctx%>/sistema_pedidos/estilosPedidos/tema.css">

    <script src="<%=ctx%>/sistema_pedidos/js/tema.js"></script>

    <style>
        body {
            min-height: 100vh;
        }

        .encabezado-pedido {
            background: linear-gradient(135deg, #153e52, #087e8b);
            color: white;
            padding: 30px;
            border-radius: 16px;
        }

        .card {
            border-radius: 14px;
        }

        .table th,
        .table td {
            text-align: center;
            vertical-align: middle;
            padding: 15px;
        }

        .total-pedido {
            font-size: 28px;
            font-weight: 700;
            color: var(--rosita-green, #087e8b);
        }
    </style>

</head>

<body>

<div class="container py-5">

    <a href="<%=ctx%>/HistorialPedidos"
       class="btn btn-outline-primary mb-4">

        <i class="bi bi-arrow-left me-1"></i>
        Volver al historial

    </a>

    <div class="encabezado-pedido mb-4">

        <h2>

            <i class="bi bi-bag-check me-2"></i>
            Detalle de mi pedido

        </h2>

        <p class="mb-0">
            Consulta la información de tu compra.
        </p>

    </div>

    <!-- INFORMACIÓN DEL PEDIDO -->

    <div class="card shadow-sm mb-4">

        <div class="card-body p-4">

            <h5 class="fw-bold mb-4">
                Información del pedido
            </h5>

            <div class="row g-3">

                <div class="col-md-6">
                    <small class="text-muted">Número de pedido</small>

                    <p class="fw-bold">
                        <%=esc(pedido.getNumero())%>
                    </p>
                </div>

                <div class="col-md-6">
                    <small class="text-muted">Fecha</small>

                    <p class="fw-bold">
                        <%=esc(pedido.getCreadoEn())%>
                    </p>
                </div>

                <div class="col-md-4">
                    <small class="text-muted">Tipo de entrega</small>

                    <p><%=esc(pedido.getTipoEntrega())%></p>
                </div>

                <div class="col-md-4">
                    <small class="text-muted">Método de pago</small>

                    <p><%=esc(pedido.getMetodoPago())%></p>
                </div>

                <div class="col-md-4">
                    <small class="text-muted">Estado</small>

                    <p>
                        <span class="badge text-bg-primary">
                            <%=esc(pedido.getEstado())%>
                        </span>
                    </p>
                </div>

                <% if ("DELIVERY".equals(pedido.getTipoEntrega())) { %>

                    <div class="col-12">

                        <small class="text-muted">
                            Dirección de entrega
                        </small>

                        <p>
                            <%=esc(pedido.getDireccionEntrega())%>
                        </p>

                    </div>

                <% } %>

            </div>

        </div>

    </div>

    <!-- PRODUCTOS COMPRADOS -->

    <div class="card shadow-sm mb-4">

        <div class="card-body p-4">

            <h5 class="fw-bold mb-3">
                Productos de mi pedido
            </h5>

            <div class="table-responsive">

                <table class="table table-hover">

                    <thead>

                        <tr>
                            <th>Producto</th>
                            <th>Cantidad</th>
                            <th>Precio unitario</th>
                            <th>Subtotal</th>
                        </tr>

                    </thead>

                    <tbody>

                        <% for (PedidoDetalle d : detalles) { %>

                            <tr>

                                <td><%=esc(d.getNombreProducto())%></td>

                                <td><%=d.getCantidad()%></td>

                                <td>
                                    S/ <%=String.format(
                                        Locale.US, "%.2f",
                                        d.getPrecioUnitario())%>
                                </td>

                                <td>
                                    S/ <%=String.format(
                                        Locale.US, "%.2f",
                                        d.getSubtotal())%>
                                </td>

                            </tr>

                        <% } %>

                    </tbody>

                </table>

            </div>

        </div>

    </div>

    <!-- RESUMEN ECONÓMICO -->

    <div class="card shadow-sm">

        <div class="card-body p-4">

            <h5 class="fw-bold mb-4">
                Resumen del pedido
            </h5>

            <div class="d-flex justify-content-between mb-3">

                <span>Subtotal</span>

                <strong>
                    S/ <%=String.format(
                        Locale.US, "%.2f",
                        pedido.getSubtotal())%>
                </strong>

            </div>

            <div class="d-flex justify-content-between mb-3">

                <span>Costo de delivery</span>

                <strong>
                    S/ <%=String.format(
                        Locale.US, "%.2f",
                        pedido.getCostoDelibery())%>
                </strong>

            </div>

            <hr>

            <div class="d-flex justify-content-between align-items-center">

                <h5>Total del pedido</h5>

                <span class="total-pedido">

                    S/ <%=String.format(
                        Locale.US, "%.2f",
                        pedido.getTotalEstimado())%>

                </span>

            </div>

        </div>

    </div>

</div>

</body>
</html>