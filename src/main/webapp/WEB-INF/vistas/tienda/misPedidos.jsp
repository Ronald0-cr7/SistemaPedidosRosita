<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Modelo.Pedido"%>
<%@page import="Modelo.Cliente"%>
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

    Cliente cliente = (Cliente)
            session.getAttribute("clienteLogueado");

    List<Pedido> pedidos = (List<Pedido>)
            request.getAttribute("pedidos");

    if (cliente == null) {
        response.sendRedirect(ctx + "/LoginCliente");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Historial de pedidos | Minimarket Rosita</title>

    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">

    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet"
      href="${pageContext.request.contextPath}/sistema_pedidos/estilosPedidos/tema.css">

<script src="${pageContext.request.contextPath}/sistema_pedidos/js/tema.js"></script>
    <style>

        body {
            background: #f5f8f5;
            font-family: Arial, sans-serif;
        }

        .navbar {
            background: white;
            box-shadow: 0 2px 10px #0000000c;
        }

        .marca {
            color: #378343;
            font-size: 22px;
            font-weight: bold;
        }

        .encabezado {
            background: linear-gradient(135deg, #225e30, #378343);
            color: white;
            padding: 35px;
            border-radius: 16px;
            margin-bottom: 30px;
        }

        .card {
            border: 1px solid #e0e8e0;
            border-radius: 14px;
            overflow: hidden;
        }

        .table th, .table td {
            text-align: center;
            vertical-align: middle;
            padding: 16px;
        }

        .table thead th {
            background: #edf5ed;
            color: #225e30;
            font-size: 13px;
            white-space: nowrap;
        }

        .numero-pedido {
            font-size: 13px;
            font-weight: bold;
            color: #378343;
            overflow-wrap: anywhere;
        }

        .badge {
            padding: 9px 14px;
            border-radius: 20px;
        }

        .btn-success {
            background: #378343;
            border-color: #378343;
        }

    </style>

</head>

<body>

<!-- BARRA DE NAVEGACIÓN -->

<nav class="navbar">

    <div class="container">

        <a class="navbar-brand marca"
           href="<%=ctx%>/IndexSistema">

            <i class="bi bi-basket-fill"></i>
            Minimarket Rosita

        </a>

        <a href="<%=ctx%>/IndexSistema"
           class="btn btn-outline-success">

            <i class="bi bi-house"></i>
            Volver al inicio

        </a>

    </div>

</nav>

<div class="container py-5">

    <!-- BIENVENIDA -->

    <div class="encabezado">

        <h2 class="fw-bold">

            <i class="bi bi-clock-history me-2"></i>
            Historial de pedidos

        </h2>

        <p class="mb-0">

            Hola, <strong><%=esc(cliente.getNombres())%></strong>.
            Consulta tus pedidos realizados y su estado actual.

        </p>

    </div>

    <!-- CANTIDAD DE PEDIDOS -->

    <div class="d-flex justify-content-between
                align-items-center mb-3">

        <h5 class="fw-bold mb-0">

            Mis compras

            <span class="badge text-bg-success">

                <%=pedidos != null ? pedidos.size() : 0%>

            </span>

        </h5>

        <a href="<%=ctx%>/ListaProductos"
           class="btn btn-success">

            <i class="bi bi-cart-plus"></i>
            Seguir comprando

        </a>

    </div>

    <!-- TABLA DE PEDIDOS -->

    <div class="card shadow-sm">

        <div class="card-body">

            <% if (pedidos == null || pedidos.isEmpty()) { %>

                <div class="text-center py-5">

                    <i class="bi bi-bag-x text-secondary"
                       style="font-size: 65px;"></i>

                    <h4 class="mt-3">
                        Todavía no tienes pedidos
                    </h4>

                    <p class="text-secondary">
                        Tus próximas compras aparecerán aquí.
                    </p>

                    <a href="<%=ctx%>/ListaProductos"
                       class="btn btn-success">

                        Explorar productos

                    </a>

                </div>

            <% } else { %>

                <div class="table-responsive">

                    <table class="table table-hover mb-0">

                        <thead>

                            <tr>
                                <th>N° PEDIDO</th>
                                <th>FECHA</th>
                                <th>ENTREGA</th>
                                <th>MÉTODO DE PAGO</th>
                                <th>SUBTOTAL</th>
                                <th>DELIVERY</th>
                                <th>TOTAL</th>
                                <th>ESTADO</th>
                                <th>ACCION</th>
                            </tr>

                        </thead>

                        <tbody>

                            <% for (Pedido p : pedidos) { %>

                            <tr>

                                <td class="numero-pedido">

                                    <%=esc(p.getNumero())%>

                                </td>

                                <td>
                                    <%=esc(p.getCreadoEn())%>
                                </td>

                                <td>
                                    <%=esc(p.getTipoEntrega())%>
                                </td>

                                <td>
                                    <%=esc(p.getMetodoPago())%>
                                </td>

                                <td>
                                    S/ <%=String.format(
                                        Locale.US, "%.2f",
                                        p.getSubtotal())%>
                                </td>

                                <td>
                                    S/ <%=String.format(
                                        Locale.US, "%.2f",
                                        p.getCostoDelibery())%>
                                </td>

                                <td class="fw-bold text-success">

                                    S/ <%=String.format(
                                        Locale.US, "%.2f",
                                        p.getTotalEstimado())%>

                                </td>

                                <td>

                                    <% if ("Pendiente".equals(p.getEstado())) { %>

                                        <span class="badge text-bg-warning">
                                            Pendiente
                                        </span>

                                    <% } else if ("Confirmado".equals(p.getEstado())) { %>

                                        <span class="badge text-bg-primary">
                                            Confirmado
                                        </span>

                                    <% } else if ("Atendido".equals(p.getEstado())) { %>

                                        <span class="badge text-bg-success">
                                            Atendido
                                        </span>

                                    <% } else if ("Cancelado".equals(p.getEstado())) { %>

                                        <span class="badge text-bg-danger">
                                            Cancelado
                                        </span>

                                    <% } %>

                                </td>
<td>

    <a href="<%=ctx%>/DetallePedidoCliente?id=<%=esc(p.getId())%>"
       class="btn btn-outline-primary btn-sm">

        <i class="bi bi-eye me-1"></i>
        Ver detalle

    </a>

</td>
                            </tr>

                            <% } %>

                        </tbody>

                    </table>

                </div>

            <% } %>

        </div>

    </div>

</div>

</body>
</html>