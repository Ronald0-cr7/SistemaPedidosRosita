<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Modelo.Carrito"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@page import="Modelo.Cliente"%>
<%!
    private String esc(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>
<%
    String ctx = request.getContextPath();
    Cliente cliente = (Cliente) session.getAttribute("clienteLogueado");
    String recursos = ctx + "/sistema_pedidos";
    List<Carrito> productosCarrito = (List<Carrito>) request.getAttribute("productosCarrito");
    if (productosCarrito == null) productosCarrito = java.util.Collections.emptyList();
    Number subtotalAtributo = (Number) request.getAttribute("subtotal");
    double subtotal = subtotalAtributo == null ? 0.0 : subtotalAtributo.doubleValue();
    Number cantidadAtributo = (Number) request.getAttribute("cantidadProductos");
    int cantidadProductos = cantidadAtributo == null ? 0 : cantidadAtributo.intValue();
    boolean vacio = productosCarrito.isEmpty();
%>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Carrito y pedido | Minimarket Rosita</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="<%=recursos%>/estilosPedidos/app.css" rel="stylesheet">
    <link href="<%=recursos%>/estilosPedidos/pages/carrito.css" rel="stylesheet">
    <link href="<%=ctx%>/css/theme.css?v=20261107" rel="stylesheet">
    <link rel="stylesheet"
      href="${pageContext.request.contextPath}/sistema_pedidos/estilosPedidos/tema.css">

<script src="${pageContext.request.contextPath}/sistema_pedidos/js/tema.js"></script>
    <style>
    #cartItems > .rosita-cart-item {
        display: block;
        padding: 14px;
        margin-bottom: 10px;
        border: 1px solid #354760;
        border-radius: 12px;
        background: #1b2a40;
    }

    #cartItems .rosita-cart-row {
        display: grid;
        grid-template-columns: 78px minmax(0, 1fr) auto;
        align-items: center;
        gap: 16px;
    }

    #cartItems .rosita-cart-row > .flex-grow-1 {
        min-width: 0;
        overflow-wrap: anywhere;
    }

    #cartItems .rosita-cart-row > .text-end > strong {
        color: #5eead4;
        font-size: 1.05rem;
        white-space: nowrap;
    }

    #cartItems .rosita-cart-row .btn-outline-secondary {
        min-width: 32px;
        color: #e2e8f0;
        border-color: #64748b;
    }

    @media (max-width: 575.98px) {
        #cartItems .rosita-cart-row {
            grid-template-columns: 64px minmax(0, 1fr);
            gap: 10px;
        }

        #cartItems .rosita-cart-row > img {
            width: 64px;
            height: 64px;
        }

        #cartItems .rosita-cart-row > .text-end {
            grid-column: 1 / -1;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-top: 1px solid #354760;
            padding-top: 10px;
        }

        #cartItems .rosita-cart-row > .text-end > form {
            margin-bottom: 0 !important;
        }
    }
</style>
    <script src="<%=ctx%>/js/theme.js?v=20261107"></script>
</head>
<body class="orders-page">
    <div class="topbar py-2 no-print">
        <div class="container d-flex justify-content-between">
            <span><i class="bi bi-shield-check me-2"></i>Pedido coordinado directamente con Rosita</span>
            <span class="d-none d-sm-inline"><i class="bi bi-whatsapp me-2"></i>Atención por WhatsApp</span>
        </div>
    </div>
    <header class="site-header sticky-top bg-white shadow-sm no-print">
        <nav class="navbar navbar-expand-lg">
            <div class="container py-2">
                <a class="navbar-brand brand" href="<%=ctx%>/IndexSistema">
                    <span class="brand-mark"><i class="bi bi-basket2-fill"></i></span>
                    <span><strong>Minimarket</strong><small>Rosita</small></span>
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav" aria-label="Abrir menú">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="mainNav">
                    <ul class="navbar-nav ms-auto">
                        <li class="nav-item"><a class="nav-link" href="<%=ctx%>/ListaProductos"><i class="bi bi-grid me-1"></i>Seguir comprando</a></li>
                        <li class="nav-item"><a class="nav-link" href="<%=ctx%>/HistorialPedidos"><i class="bi bi-clock-history me-1"></i>Mis pedidos</a></li>
                    </ul>
                </div>
            </div>
        </nav>
    </header>

    <main class="checkout-page no-print">
        <div class="container py-5">
            <div class="checkout-title">
                <div><span class="eyebrow">Finaliza tu compra</span><h1>Carrito y pedido</h1></div>
                <div class="checkout-steps"><span class="active">1. Carrito</span><span>2. Datos</span><span>3. WhatsApp</span></div>
            </div>

            <div id="emptyCart" class="empty-cart <%=vacio ? "" : "d-none"%>">
                <i class="bi bi-cart-x"></i>
                <h2>Tu carrito está vacío</h2>
                <p>Agrega algunos productos antes de realizar tu pedido.</p>
                <a href="<%=ctx%>/ListaProductos" class="btn btn-primary">Explorar productos</a>
            </div>

            <div id="checkoutContent" class="row g-4 <%=vacio ? "d-none" : ""%>">
                <div class="col-lg-7">
                    <section class="checkout-panel">
                        <div class="panel-heading">
                            <h2>Productos seleccionados</h2>
                            <span id="cartItemCount"><%=cantidadProductos%> <%=cantidadProductos == 1 ? "unidad" : "unidades"%></span>
                        </div>
                        <div id="cartItems" class="cart-items">
                            <% for (Carrito item : productosCarrito) {
                                String imagen = item.getImagen();
                                if (imagen == null || imagen.isBlank()) {
                                    imagen = ctx + "/img/producto.svg";
                                } else if (imagen.startsWith("/")) {
                                    imagen = ctx + imagen;
                                } else if (!imagen.startsWith("https://") && !imagen.startsWith("http://")) {
                                    imagen = ctx + "/" + imagen;
                                }
                            %>
                            <article class="cart-item rosita-cart-item">
                                <div class="rosita-cart-row">
                                    <img src="<%=esc(imagen)%>" alt="<%=esc(item.getNombre())%>"
                                         width="78" height="78" style="object-fit:contain;border-radius:10px;background:#fff;">
                                    <div class="flex-grow-1">
                                        <h3 class="h6 mb-1"><%=esc(item.getNombre())%></h3>
                                        <small class="text-muted"><%=esc(item.getCategoria())%></small>
                                        <div class="small">S/ <%=String.format(Locale.US, "%.2f", item.getPrecio())%> c/u</div>
                                        <form action="<%=ctx%>/EliminarCarrito" method="POST" class="mt-2">
                                            <input type="hidden" name="productoId" value="<%=item.getProductoId()%>">
                                            <button type="submit" class="btn btn-sm btn-outline-danger">
                                                <i class="bi bi-trash"></i> Quitar
                                            </button>
                                        </form>
                                    </div>
                                    <div class="text-end">
                                        <form action="<%=ctx%>/ActualizarCarrito" method="POST" class="d-flex align-items-center justify-content-end gap-2 mb-2">
                                            <input type="hidden" name="productoId" value="<%=item.getProductoId()%>">
                                            <button type="submit" name="cantidad" value="<%=item.getCantidad()-1%>" class="btn btn-sm btn-outline-secondary" aria-label="Disminuir cantidad">−</button>
                                            <strong><%=item.getCantidad()%></strong>
                                            <button type="submit" name="cantidad" value="<%=item.getCantidad()+1%>"
                                                    class="btn btn-sm btn-outline-secondary"
                                                    <%=item.getCantidad() >= item.getStockDisponible() ? "disabled" : ""%>
                                                    aria-label="Aumentar cantidad">+</button>
                                        </form>
                                        <strong>S/ <%=String.format(Locale.US, "%.2f", item.getSubtotal())%></strong>
                                    </div>
                                </div>
                            </article>
                            <% } %>
                        </div>
                        <div class="d-flex justify-content-end mt-3">
                            <form action="<%=ctx%>/VaciarCarrito" method="POST" onsubmit="return confirm('¿Vaciar todo el carrito?');">
                                <button type="submit" class="btn btn-outline-danger btn-sm"><i class="bi bi-trash3 me-1"></i>Vaciar carrito</button>
                            </form>
                        </div>
                    </section>

                    <section class="checkout-panel mt-4">
                        <div class="panel-heading"><h2>Datos para tu pedido</h2><span>Campos obligatorios</span></div>
                        <form id="checkoutForm" class="needs-validation"
      action="<%= ctx %>/RegistrarPedidoCarrito" method="post">
                            
    <input type="hidden" name="csrf"
           value="${sessionScope.csrfCarrito}">

    <div class="row g-3">
        <div class="col-md-6">

    <label class="form-label" for="customerName">
        Nombre completo
    </label>

    <input class="form-control"
           id="customerName"
           name="nombre_contacto"
           autocomplete="name"
           value="<%=cliente != null
               ? esc(cliente.getNombres() + " " + cliente.getApellidos())
               : ""%>"
           required>

    <div class="invalid-feedback">Ingresa tu nombre.</div>

</div>

<div class="col-md-6">

    <label class="form-label" for="customerPhone">
        Celular / WhatsApp
    </label>

    <input class="form-control"
           id="customerPhone"
           name="telefono_contacto"
           type="tel"
           inputmode="numeric"
           pattern="[0-9]{9}"
           maxlength="9"
           placeholder="987654321"
           value="<%=cliente != null
               ? esc(cliente.getTelefono()) : ""%>"
           required>

    <div class="invalid-feedback">
        Ingresa un celular de 9 dígitos.
    </div>

</div>
                                
                                <div class="col-12">
                                    <label class="form-label">Tipo de entrega</label>
                                    <div class="delivery-options">
                                        <label><input type="radio" name="tipo_entrega" value="RECOJO" checked>
                                            <span><i class="bi bi-shop"></i><strong>Recojo en tienda</strong><small>Sin costo adicional</small></span>
                                        </label>
                                        <label><input type="radio" name="tipo_entrega" value="DELIVERY">
                                            <span><i class="bi bi-bicycle"></i><strong>Delivery</strong><small>Adicional S/ 5.00</small></span>
                                        </label>
                                    </div>
                                </div>
                                <div id="deliveryFields" class="col-12 d-none">
                                    <div class="delivery-box">
                                        <div class="mb-3">
                                            <label class="form-label" for="deliveryAddress">Dirección de entrega</label>
                                            <input class="form-control" id="deliveryAddress" name="direccion_entrega" placeholder="Calle, número, urbanización y referencia">
                                            <div class="invalid-feedback">Ingresa la dirección de entrega.</div>
                                        </div>
                                        <div class="location-row">
                                            <button id="getLocation" class="btn btn-outline-primary" type="button"><i class="bi bi-crosshair me-1"></i>Usar mi ubicación</button>
                                            <span id="locationStatus">También puedes compartir tus coordenadas.</span>
                                        </div>
                                        <input id="latitude" name="latitud" type="hidden">
                                        <input id="longitude" name="longitud" type="hidden">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="paymentMethod">Método de pago</label>
                                    <select class="form-select" id="paymentMethod" name="metodo_pago" required>
                                        <option value="">Selecciona</option>
                                        <option value="EFECTIVO">EFECTIVO</option>
                                        <option value="YAPE">YAPE</option>
                                        <option value="PLIN">PLIN</option>
                                        <option value="TARJETA">TARJETA</option>
                                        <option value="OTRO">OTRO</option>
                                    </select>
                                    <div class="invalid-feedback">Selecciona un método.</div>
                                </div>
                                <div class="col-12">
                                    <label class="form-label" for="orderNotes">Indicaciones del pedido</label>
                                    <textarea class="form-control" id="orderNotes" name="observaciones" rows="3" maxlength="500"
                                              placeholder="Referencia, vuelto, sabor, presentación u otra indicación"></textarea>
                                </div>
                            </div>
                        </form>
                    </section>
                </div>

                <div class="col-lg-5">
                    <aside class="order-summary">
                        <h2>Resumen</h2>
                        <div class="summary-line"><span>Productos</span><strong id="subtotal">S/ <%=String.format(Locale.US, "%.2f", subtotal)%></strong></div>
                        <div class="summary-line"><span>Delivery</span><strong id="deliveryCost">S/ 0.00</strong></div>
                        <div class="summary-total"><span>Total estimado</span><strong id="orderTotal">S/ <%=String.format(Locale.US, "%.2f", subtotal)%></strong></div>
                        <p class="summary-note"><i class="bi bi-info-circle"></i>El administrador confirmará disponibilidad y total final por WhatsApp.</p>
<button id="placeOrder" class="btn btn-whatsapp btn-lg w-100"
        type="submit" form="checkoutForm">
    <i class="bi bi-whatsapp me-2"></i>Enviar pedido por WhatsApp
</button>
                        <p id="orderMessage" class="small mt-2" role="status"></p>
                        <a href="<%=ctx%>/MisPedidos" class="history-link"><i class="bi bi-clock-history"></i>Consultar historial de pedidos</a>
                    </aside>
                </div>
            </div>
        </div>
    </main>

    <!-- Estructura del voucher conservada para conectar al futuro registro de pedidos -->
    <section id="voucher" class="voucher d-none" aria-label="Voucher de pedido">
        <div class="voucher-brand"><i class="bi bi-basket2-fill"></i><h1>Minimarket Rosita</h1><p>Comprobante de pedido web</p></div>
        <div class="voucher-meta"><span>Pedido: <strong id="voucherNumber"></strong></span><span>Fecha: <strong id="voucherDate"></strong></span></div>
        <div id="voucherCustomer"></div>
        <table><thead><tr><th>Producto</th><th>Cant.</th><th>Importe</th></tr></thead><tbody id="voucherItems"></tbody></table>
        <div id="voucherTotals"></div>
        <p class="voucher-footer">Este documento registra una solicitud. El pago y la disponibilidad serán confirmados por Minimarket Rosita.</p>
        <div class="voucher-actions no-print">
            <button id="printVoucher" class="btn btn-dark" type="button"><i class="bi bi-printer me-2"></i>Imprimir voucher</button>
            <button id="closeVoucher" class="btn btn-outline-secondary" type="button">Cerrar</button>
        </div>
    </section>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
    document.addEventListener('DOMContentLoaded', function () {
        const subtotal = <%=String.format(Locale.US, "%.2f", subtotal)%>;
        const form = document.getElementById('checkoutForm');
        const deliveryFields = document.getElementById('deliveryFields');
        const deliveryAddress = document.getElementById('deliveryAddress');
        const deliveryCost = document.getElementById('deliveryCost');
        const orderTotal = document.getElementById('orderTotal');
        const orderMessage = document.getElementById('orderMessage');
        const format = n => 'S/ ' + n.toFixed(2);

        function actualizarEntrega() {
            const delivery = form.querySelector('input[name="tipo_entrega"]:checked').value === 'DELIVERY';
            deliveryFields.classList.toggle('d-none', !delivery);
            deliveryAddress.required = delivery;
            deliveryCost.textContent = format(delivery ? 5 : 0);
            orderTotal.textContent = format(subtotal + (delivery ? 5 : 0));
        }
        form.querySelectorAll('input[name="tipo_entrega"]').forEach(radio => {
            radio.addEventListener('change', actualizarEntrega);
        });
        actualizarEntrega();

        document.getElementById('getLocation').addEventListener('click', function () {
            const status = document.getElementById('locationStatus');
            if (!navigator.geolocation) {
                status.textContent = 'Tu navegador no permite obtener la ubicación.';
                return;
            }
            status.textContent = 'Solicitando ubicación...';
            navigator.geolocation.getCurrentPosition(pos => {
                document.getElementById('latitude').value = pos.coords.latitude;
                document.getElementById('longitude').value = pos.coords.longitude;
                status.textContent = 'Ubicación obtenida. Completa también la dirección.';
            }, () => {
                status.textContent = 'No se pudo obtener la ubicación. Ingresa la dirección manualmente.';
            });
        });

        document.getElementById('placeOrder').addEventListener('click', function () {
            form.classList.add('was-validated');
            if (!form.checkValidity()) {
                orderMessage.textContent = 'Completa los campos obligatorios antes de continuar.';
                return;
            }
          orderMessage.textContent = 'Registrando tu pedido...';
        });

        document.getElementById('printVoucher').addEventListener('click', () => window.print());
        document.getElementById('closeVoucher').addEventListener('click', () => {
            document.getElementById('voucher').classList.add('d-none');
        });
    });
    </script>
    <%-- No cargar el antiguo js/pages/carrito.js: gestiona un carrito distinto en localStorage. --%>
</body>
</html>
