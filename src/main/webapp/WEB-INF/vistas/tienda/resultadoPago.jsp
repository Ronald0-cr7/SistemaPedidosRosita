<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Modelo.Pago"%>

<%
    String ctx = request.getContextPath();

    Pago pago = (Pago) request.getAttribute("pago");

    String estado =
        (String) request.getAttribute("estadoPago");

    if (pago == null || estado == null) {
        response.sendError(404);
        return;
    }

    boolean aprobado = "Aprobado".equals(estado);

    boolean pendiente = "Pendiente".equals(estado);

    String color = aprobado ? "success"
                   : pendiente ? "warning" : "danger";

    String icono = aprobado ? "bi-check-circle-fill"
                   : pendiente ? "bi-hourglass-split"
                   : "bi-x-circle-fill";

    String titulo = aprobado ? "¡Pago realizado correctamente!"
                    : pendiente ? "Pago pendiente de confirmación"
                    : "El pago no está acreditado";
%>

<!DOCTYPE html>
<html lang="es">

<head>

    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Resultado del pago | Minimarket Rosita</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
          rel="stylesheet">

    <style>

        body {
            min-height: 100vh;
            background: #f1f5f9;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .tarjeta {
            width: 100%;
            max-width: 520px;
            background: white;
            padding: 40px;
            border-radius: 24px;
            box-shadow: 0 15px 40px rgba(0,0,0,.08);
            text-align: center;
        }

        .icono {
            font-size: 75px;
        }

        .detalle {
            background: #f8fafc;
            padding: 20px;
            border-radius: 16px;
            margin: 25px 0;
        }

    </style>

</head>

<body>

    <main class="tarjeta">

        <h6 class="text-muted">MINIMARKET ROSITA</h6>

        <i class="bi <%=icono%> icono text-<%=color%>"></i>

        <h3 class="fw-bold mt-3">
            <%=titulo%>
        </h3>

        <div class="detalle">

            <p class="text-muted mb-1">Importe del pedido</p>

            <h2 class="fw-bold">
                S/ <%=pago.getMonto().toPlainString()%>
            </h2>

            <span class="badge bg-<%=color%>">
                <%=estado%>
            </span>

        </div>

        <% if (aprobado) { %>

            <p>Mercado Pago confirmó tu pago.
               Gracias por tu compra.</p>

        <% } else if (pendiente) { %>

            <p>Estamos verificando tu pago.
               No realices otro pago mientras esté pendiente.</p>

        <% } else { %>

            <p>Consulta el estado de tu pedido
               antes de volver a pagar.</p>

        <% } %>

        <a href="<%=ctx%>/MisPedidos"
           class="btn btn-primary w-100 mb-2">
            <i class="bi bi-receipt"></i>
            Ver mis pedidos
        </a>

        <a href="<%=ctx%>/IndexSistema"
           class="btn btn-outline-secondary w-100">
            Volver a la tienda
        </a>

    </main>

</body>

</html>