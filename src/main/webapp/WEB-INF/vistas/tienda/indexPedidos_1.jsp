<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Modelo.Producto"%>
<%@page import="Modelo.Tienda"%>
<%@page import="java.util.List"%>
<%!
    // Evita que el texto de la base de datos se interprete como HTML.
    String texto(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>
<%
    String ctx = request.getContextPath();
    String recursos = ctx + "/sistema_pedidos";
    Tienda tienda = (Tienda) request.getAttribute("infoTienda");
    List<Producto> productos = (List<Producto>) request.getAttribute("productosCatalogo");

    String nombre = tienda != null ? texto(tienda.getNombre()) : "Minimarket Rosita";
    String direccion = tienda != null ? texto(tienda.getDireccion()) : "";
    String telefono = tienda != null ? texto(tienda.getWhatsapp()) : "";
    String whatsapp = tienda != null && tienda.getWhatsapp() != null
            ? tienda.getWhatsapp().replaceAll("[^0-9]", "") : "";
    String mapa = tienda != null ? tienda.getMaps() : null;
    String foto = tienda != null ? tienda.getImagen() : null;
%>
<!doctype html>
<html lang="es">
 
<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <meta name="description"
          content="Minimarket Rosita: productos frescos y esenciales para tu hogar.">

    <title>Minimarket Rosita | Pagina principal</title>

<!-- GOOGLE FONTS -->
<link rel="preconnect"
      href="https://fonts.googleapis.com">

<link rel="preconnect"
      href="https://fonts.gstatic.com"
      crossorigin>

<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
      rel="stylesheet">

<!-- BOOTSTRAP -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet">

<!-- BOOTSTRAP ICONS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"
      rel="stylesheet">

<!-- ESTILOS DE MINIMARKET ROSITA -->
<link href="<%=recursos%>/estilosPedidos/vendor.css"
      rel="stylesheet">

<link href="<%=recursos%>/estilosPedidos/style.css"
      rel="stylesheet">

<link href="<%=recursos%>/estilosPedidos/app.css"
      rel="stylesheet">

<!-- ESTILOS ESPECIFICOS DE INICIO -->
<link href="<%=recursos%>/estilosPedidos/pages/inicio.css"
      rel="stylesheet">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/sistema_pedidos/estilosPedidos/tema.css">

<script src="${pageContext.request.contextPath}/sistema_pedidos/js/tema.js"></script>

</head>

    <body>
        <div class="topbar py-2">
            <div class="container d-flex flex-wrap justify-content-between gap-2">
                <span>
                    <i class="bi bi-geo-alt me-2">
                    </i>
                    Atención local en tu barrio
                </span>
                <span>
                    <i class="bi bi-telephone me-2">
                    </i>
                    Pedidos:
                    <span><%= telefono %></span>
                </span>
            </div>
        </div>
        <header class="site-header sticky-top bg-white shadow-sm">
            <nav class="navbar navbar-expand-lg" aria-label="Navegación principal">
                <div class="container py-2">
                    <a class="navbar-brand brand" href="<%=ctx%>/IndexSistema" aria-label="Minimarket Rosita, inicio">
                        <span class="brand-mark">
                            <i class="bi bi-basket2-fill">
                            </i>
                        </span>
                        <span>
                            <strong>
                                Minimarket
                            </strong>
                            <small>
                                Rosita
                            </small>
                        </span>
                    </a>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav" aria-controls="mainNav" aria-expanded="false" aria-label="Abrir menú">
                        <span class="navbar-toggler-icon">
                        </span>
                    </button>
                    <div class="collapse navbar-collapse" id="mainNav">
                        <ul class="navbar-nav mx-auto gap-lg-2">
                            <li class="nav-item">
                                <a class="nav-link active" aria-current="page" href="<%=ctx%>/IndexSistema">
                                    Inicio
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<%=ctx%>/ListaProductos">
                                    Productos
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#categorias">
                                    Categorías
                                </a>
                            </li>
                             <li class="nav-item">
                                <a class="nav-link" href="<%=ctx%>/Carrito">
                                    Carrito de compras
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<%=ctx%>/SVCarrito">
                                    Mis pedidos
                                </a>
                            </li>
                              <li class="nav-item">
                                <a class="nav-link" href="<%=ctx%>/HistorialPedidos">
                                    Historial pedidos
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#nosotros">
                                    Nosotros
                                </a>
                            </li>
                        </ul>
                     <div class="d-flex align-items-center gap-2 mt-3 mt-lg-0">

    <% if (session.getAttribute("clienteLogueado") == null) { %>

        <a class="btn btn-outline-primary"
           href="<%=ctx%>/LoginCliente">
            <i class="bi bi-person me-1"></i>
            Ingresar
        </a>

        <a class="btn btn-primary"
           href="<%=ctx%>/RegistroCliente">
            Crear cuenta
        </a>

    <% } else { %>

        <span class="fw-bold text-success">
            <i class="bi bi-person-circle"></i>
            Mi cuenta
        </span>

        <form action="<%=ctx%>/CerrarSesionCliente" method="POST">

            <input type="hidden" name="csrf"
                   value="${sessionScope.csrfLogin}">

            <button type="submit" class="btn btn-outline-danger">
                Cerrar sesión
            </button>

        </form>

    <% } %>

</div>
                    </div>
                </div>
            </nav>
        </header>
        <main>
            <section class="hero position-relative">
                <div id="heroCarousel" class="carousel slide">
                    <div class="carousel-inner">
                        <article class="carousel-item active hero-slide">
                            <img src="<%=recursos%>/images/hero-minimarket-rosita.png" class="d-block w-100" alt="Interior surtido del Minimarket Rosita con bebidas, snacks, helados y abarrotes">
                            <div class="hero-overlay">
                            </div>
                            <div class="carousel-caption container text-start">
                                <p class="hero-kicker">
                                    Tu minimarket de confianza
                                </p>
                                <h1>
                                    Un poco de todo, cerquita de ti
                                </h1>
                                <p class="hero-copy">
                                    Helados, snacks, gaseosas, bebidas, abarrotes, cervezas y mucho más para cada momento.
                                </p>
                                <a href="<%=ctx%>/ListaProductos" class="btn btn-primary btn-lg">
                                    Ver productos
                                    <i class="bi bi-arrow-right ms-1">
                                    </i>
                                </a>
                            </div>
                        </article>
                    </div>
                </div>
                <div class="container hero-search-wrap">
                    <form id="heroSearch" class="hero-search" action="<%=ctx%>/ListaProductos" method="get">
                        <i class="bi bi-search">
                        </i>
                        <label class="visually-hidden" for="heroSearchInput">
                            Buscar productos
                        </label>
                        <input id="heroSearchInput" name="q" type="search" placeholder="¿Qué producto estás buscando?" required>
                        <button class="btn btn-primary" type="submit">
                            Buscar
                        </button>
                    </form>
                </div>
            </section>
            <section id="categorias" class="section-space">
                <div class="container">
                    <div class="section-heading">
                        <div>
                            <span class="eyebrow">
                                Compra fácil
                            </span>
                            <h2>
                                Categorías principales
                            </h2>
                        </div>
                      <a href="<%=ctx%>/ListaProductos" class="text-link">
    Ver todas
    <i class="bi bi-arrow-right"></i>
</a>
                    </div>


<div class="category-grid">

    <!-- HELADOS - ID 7 -->
    <a class="category-item category-ice"
       href="<%=ctx%>/ProductosPorCategoria?categoria=7">
        <span>
            <i class="bi bi-snow2"></i>
        </span>
        <strong>Helados</strong>
        <small>Para refrescarte</small>
    </a>

    <!-- SNACKS - ID 5 -->
    <a class="category-item category-snack"
       href="<%=ctx%>/ProductosPorCategoria?categoria=5">
        <span>
            <i class="bi bi-cookie"></i>
        </span>
        <strong>Snacks</strong>
        <small>Antojos para compartir</small>
    </a>

    <!-- GASEOSAS - ID 6 -->
    <a class="category-item category-soda"
       href="<%=ctx%>/ProductosPorCategoria?categoria=6">
        <span>
            <i class="bi bi-cup-straw"></i>
        </span>
        <strong>Gaseosas</strong>
        <small>Siempre bien frías</small>
    </a>

    <!-- ABARROTES - ID 1 -->
    <a class="category-item category-grocery"
       href="<%=ctx%>/ProductosPorCategoria?categoria=1">
        <span>
            <i class="bi bi-bag"></i>
        </span>
        <strong>Abarrotes</strong>
        <small>Para tu despensa</small>
    </a>

    <!-- CERVEZAS Y TRAGOS - ID 8 -->
    <a class="category-item category-beer"
       href="<%=ctx%>/ProductosPorCategoria?categoria=8">
        <span>
            <i class="bi bi-cup"></i>
        </span>
        <strong>Cervezas y tragos</strong>
        <small>Variedad para adultos</small>
    </a>

    <!-- BEBIDAS - ID 3 -->
    <a class="category-item category-drink"
       href="<%=ctx%>/ProductosPorCategoria?categoria=3">
        <span>
            <i class="bi bi-droplet"></i>
        </span>
        <strong>Bebidas</strong>
        <small>Jugos, agua y más</small>
    </a>

</div>


                </div>
            </section>
            <section id="destacados" class="section-space bg-soft">
                <div class="container">
                    <div class="section-heading">
                        <div>
                            <span class="eyebrow">
                                Los más elegidos
                            </span>
                            <h2>
                                Productos destacados
                            </h2>
                        </div>
                       <a href="<%=ctx%>/ListaProductos" class="btn btn-outline-primary">
                            Ver catálogo
                        </a>
                    </div>
            
<div class="row g-4" id="featuredProducts">

    <% if (productos != null && !productos.isEmpty()) { %>

        <% for (Producto p : productos) { %>

            <div class="col-12 col-sm-6 col-lg-3">

                <div class="card h-100 shadow-sm">

<% if (p.getImagen() != null) { %>
    <img src="<%= ctx + p.getImagen() %>"
         class="card-img-top"
         style="height:200px;object-fit:contain;">
<% } %>

                    <div class="card-body">

                        <h5><%= texto(p.getNombre()) %></h5>

                        <p><%= texto(p.getDescripcion()) %></p>

                        <h5 class="text-primary">
                            S/ <%= p.getPrecio() %>
                        </h5>

                        <p>Stock: <span data-stock="<%=p.getId()%>"><%= p.getStock() %></span></p>
                        

    <form action="<%= ctx %>/AgregarCarrito" method="POST">

        <input type="hidden"
               name="productoId"
               value="<%= p.getId() %>">

        <input type="hidden"
               name="cantidad"
               value="1">

        <button type="submit" class="btn btn-primary w-100" data-stock-button="<%=p.getId()%>" <%=p.getStock()<=0 ? "disabled" : ""%>>
            <i class="bi bi-cart-plus me-2"></i>
            Agregar al carrito
        </button>

    </form>

                    </div>

                </div>

            </div>

        <% } %>

    <% } else { %>

        <p class="text-center">
            No hay productos disponibles.
        </p>

    <% } %>

</div>

                </div>
            </section>
            <section id="nosotros" class="section-space benefits">
                <div class="container">
                    <div class="row g-4">
                        <div class="col-md-4">
                            <div class="benefit">
                                <i class="bi bi-patch-check">
                                </i>
                                <div>
                                    <h3>
                                        Calidad seleccionada
                                    </h3>
                                    <p>
                                        Revisamos nuestros productos para darte siempre una buena compra.
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="benefit">
                                <i class="bi bi-tags">
                                </i>
                                <div>
                                    <h3>
                                        Precios convenientes
                                    </h3>
                                    <p>
                                        Opciones para tu día a día y promociones para ahorrar más.
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="benefit">
                                <i class="bi bi-emoji-smile">
                                </i>
                                <div>
                                    <h3>
                                        Atención cercana
                                    </h3>
                                    <p>
                                        Somos tu minimarket de confianza, listos para ayudarte.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </main>
        <section id="informacion-tienda" class="section-space bg-soft">
            <div class="container">
                <div class="section-heading">
                    <div>
                        <span class="eyebrow">Visítanos</span>
                        <h2>Información de la tienda</h2>
                    </div>
                </div>
                <div class="row g-4">
                    <div class="col-md-6">
                      
<% if (foto != null && !foto.isBlank()) { %>

    <img src="<%= texto(
        foto.startsWith("http://") || foto.startsWith("https://")
            ? foto
            : ctx + (foto.startsWith("/") ? foto : "/" + foto)
    ) %>"
         alt="Foto de la tienda"
         class="img-fluid rounded mb-3"
         style="max-height:320px;object-fit:cover;">

<% } %>

                        <h3><%= nombre %></h3>
                        <p><i class="bi bi-geo-alt me-2"></i><%= direccion %></p>
                        <% if (mapa != null && (mapa.startsWith("https://") || mapa.startsWith("http://"))) { %>
                            <a class="btn btn-outline-primary" href="<%= texto(mapa) %>"
                               target="_blank" rel="noopener noreferrer">
                                Ver ubicación en Google Maps
                            </a>
                        <% } %>
                    </div>
                    <div class="col-md-6">
                        <h3>Haz tu pedido</h3>
                        <p>Escríbenos por WhatsApp: <%= telefono %></p>
                        <% if (!whatsapp.isEmpty()) { %>
                            <a class="btn btn-primary" href="https://wa.me/<%= whatsapp %>"
                               target="_blank" rel="noopener noreferrer">
                                <i class="bi bi-whatsapp me-2"></i>Contactar a la tienda
                            </a>
                        <% } %>
                    </div>
                </div>
            </div>
        </section>
        <footer class="site-footer">
            <div class="container py-5">
                <div class="row g-4">
                    <div class="col-lg-5">
                        <a class="brand brand-light" href="<%=ctx%>/IndexSistema">
                            <span class="brand-mark">
                                <i class="bi bi-basket2-fill">
                                </i>
                            </span>
                            <span>
                                <strong>
                                    Minimarket
                                </strong>
                                <small>
                                    Rosita
                                </small>
                            </span>
                        </a>
                        <p class="mt-3 footer-description">
                            Productos frescos y esenciales, cerca de ti.
                        </p>
                    </div>
                    <div class="col-6 col-lg-2">
                        <h3>
                            Explora
                        </h3>
                        <a href="<%=ctx%>/IndexSistema">
                            Inicio
                        </a>
                        <a href="<%=recursos%>/ListaProductos">
                            Productos
                        </a>
                        <a href="#categorias">
                            Categorías
                        </a>
                    </div>
                    <div class="col-6 col-lg-2">
                        <h3>
                            Tu cuenta
                        </h3>
                        <a href="<%=recursos%>/LoginCliente">
                            Ingresar
                        </a>
                        <a href="<%=recursos%>/RegistroCliente">
                            Registrarte
                        </a>
                        <a href="<%=recursos%>/historial.html">
                            Mis pedidos
                        </a>
                    </div>
                    <div class="col-lg-3">
                        <h3>
                            Contacto
                        </h3>
                        <p>
                            <i class="bi bi-geo-alt me-2">
                            </i>
                            <span><%= direccion %></span>
                        </p>
                        <p>
                            <i class="bi bi-telephone me-2">
                            </i>
                            <span><%= telefono %></span>
                        </p>
                    </div>
                </div>
                <div class="footer-bottom">
                    <span>
                        ©
                        <span data-current-year>
                        </span>
                        Minimarket Rosita.
                    </span>
                    <span>
                        Plantilla base por
                        <a href="https://templatesjungle.com/">
                            TemplatesJungle
                        </a>
                    </span>
                </div>
            </div>
        </footer>

<!-- BOOTSTRAP -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<!-- JAVASCRIPT DEL SISTEMA DE PEDIDOS -->
<script src="<%=recursos%>/js/app.js"></script>


        <script>
            document.querySelectorAll('[data-current-year]').forEach(el => {
            el.textContent = new Date().getFullYear();
            });
        </script>
<script src="<%=ctx%>/sistema_pedidos/js/stock-reservas.js" data-context="<%=ctx%>"></script>
</body>
</html>
