<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Modelo.Producto"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%!
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
    List<Producto> productos = (List<Producto>) request.getAttribute("productosCatalogo");
    @SuppressWarnings("unchecked")
    Map<Integer, Integer> carritoSesion = (Map<Integer, Integer>) session.getAttribute("carrito");
    long cantidadCarrito = 0;
    if (carritoSesion != null) {
        for (Integer unidades : carritoSesion.values()) {
            if (unidades != null && unidades > 0) cantidadCarrito += unidades;
        }
    }
%>
<!doctype html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Productos | Minimarket Rosita</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="<%= recursos %>/estilosPedidos/vendor.css" rel="stylesheet">
    <link href="<%= recursos %>/estilosPedidos/style.css" rel="stylesheet">
    <link href="<%= recursos %>/estilosPedidos/app.css" rel="stylesheet">
    <link href="<%= recursos %>/estilosPedidos/pages/productos.css" rel="stylesheet">
    <link rel="stylesheet"
      href="${pageContext.request.contextPath}/sistema_pedidos/estilosPedidos/tema.css">

<script src="${pageContext.request.contextPath}/sistema_pedidos/js/tema.js"></script>
</head>
<body>
    <div class="topbar py-2">
        <div class="container d-flex justify-content-between">
            <span><i class="bi bi-geo-alt me-2"></i>Atención local en tu barrio</span>
            <span class="d-none d-sm-inline"><i class="bi bi-telephone me-2"></i>Minimarket Rosita</span>
        </div>
    </div>

    <header class="site-header sticky-top bg-white shadow-sm">
        <nav class="navbar navbar-expand-lg">
            <div class="container py-2">
                <a class="navbar-brand brand" href="<%= ctx %>/IndexSistema">
                    <span class="brand-mark"><i class="bi bi-basket2-fill"></i></span>
                    <span><strong>Minimarket</strong><small>Rosita</small></span>
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                        data-bs-target="#mainNav" aria-label="Abrir menú">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="mainNav">
                    <ul class="navbar-nav mx-auto">
                        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/IndexSistema">Inicio</a></li>
                        <li class="nav-item"><a class="nav-link active" href="<%= ctx %>/ListaProductos">Productos</a></li>
                        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/IndexSistema#categorias">Categorías</a></li>
                        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/Carrito">Mis pedidos</a></li>
                     <li class="nav-item"><a class="nav-link" href="<%= ctx %>/HistorialPedidos">Historial de Pedidos</a></li>
                        <li class="nav-item"><a class="nav-link" href="<%= ctx %>/LoginCliente">Iniciar Sesion</a></li>
                      <li class="nav-item"><a class="nav-link" href="<%= ctx %>/RegistroCliente">Crear cuenta</a></li>
                          
                    
                    </ul>
                    <div class="d-flex gap-2 mt-3 mt-lg-0">
                        <a class="btn btn-outline-primary" href="<%= recursos %>/login.html">Ingresar</a>
                       <a class="btn btn-primary position-relative"
   href="<%=ctx%>/Carrito">

    <i class="bi bi-cart3 me-1"></i> Carrito

    <span class="position-absolute top-0 start-100
                 translate-middle badge rounded-pill bg-danger">
        <%= cantidadCarrito %>
    </span>

</a>
                    </div>
                </div>
            </div>
        </nav>
    </header>

    <main>
        <section class="catalog-heading">
            <div class="container">
                <nav aria-label="Migas de pan">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="<%= ctx %>/IndexSistema">Inicio</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Productos</li>
                    </ol>
                </nav>
                <h1>De todo para cada momento</h1>
                <p>Explora helados, snacks, bebidas, abarrotes, cervezas, frutas y mucho más.</p>
            </div>
        </section>

        <section class="section-space">
            <div class="container">
                <div class="catalog-toolbar">
                    <div class="search-box">
                        <i class="bi bi-search"></i>
                        <label class="visually-hidden" for="productSearch">Buscar producto</label>
                        <input id="productSearch" type="search" placeholder="Buscar por nombre o categoría">
                    </div>
                    <label class="visually-hidden" for="categoryFilter">Filtrar categoría</label>
                    <select id="categoryFilter" class="form-select">
                        <option value="">Todas las categorías</option>
                    </select>
                    <label class="visually-hidden" for="sortProducts">Ordenar productos</label>
                    <select id="sortProducts" class="form-select">
                        <option value="default">Orden recomendado</option>
                        <option value="price-asc">Menor precio</option>
                        <option value="price-desc">Mayor precio</option>
                        <option value="name">Nombre A-Z</option>
                    </select>
                </div>

                <div class="result-summary">
                    <strong id="resultCount">0 productos</strong>
                    <button id="clearFilters" class="btn btn-link" type="button">Limpiar filtros</button>
                </div>
                

                <div id="productGrid" class="row g-4">
                    <% if (productos != null) { %>
                        <% for (Producto p : productos) { %>
                            <div class="col-12 col-sm-6 col-lg-3 producto-item"
                                 data-nombre="<%= texto(p.getNombre()) %>"
                                 data-categoria="<%= texto(p.getNombreCategoria()) %>"
                                 data-precio="<%= p.getPrecio() %>">
                                <div class="card h-100 shadow-sm">
                                    <% if (p.getImagen() != null && !p.getImagen().isBlank()) { %>
                                        <img src="<%= ctx + texto(p.getImagen()) %>"
                                             class="card-img-top"
                                             alt="<%= texto(p.getNombre()) %>"
                                             style="height:200px;object-fit:contain;">
                                    <% } %>
                                    <div class="card-body">
                                        <small class="text-muted"><%= texto(p.getNombreCategoria()) %></small>
                                        <h5><%= texto(p.getNombre()) %></h5>
                                        <p><%= texto(p.getDescripcion()) %></p>
                                        <h5 class="text-primary">S/ <%= p.getPrecio() %></h5>
                                      <%
    int cantidadEnCarrito = carritoSesion == null
            ? 0
            : carritoSesion.getOrDefault(p.getId(), 0);

    int stockDisponible = Math.max(0, p.getStock() - cantidadEnCarrito);
%>

<p>Puedes agregar: <%= stockDisponible %></p>

<% if (stockDisponible > 0) { %>
<form action="<%= ctx %>/AgregarCarrito" method="post">
    <input type="hidden" name="productoId" value="<%= p.getId() %>">
    <input type="hidden" name="cantidad" value="1">

    <button type="submit" class="btn btn-primary w-100">
        <i class="bi bi-cart-plus me-2"></i>
        Agregar al carrito
    </button>
</form>

<% } else { %>

 <button type="button" class="btn btn-secondary w-100" disabled>
    <%= p.getStock() > 0 ? "Límite alcanzado" : "Sin stock" %>
</button>

<% } %>
                                    </div>
                                </div>
                            </div>
                        <% } %>
                    <% } %>
                </div>

                <div id="emptyProducts" class="empty-state d-none">
                    <i class="bi bi-search"></i>
                    <h2>No encontramos productos</h2>
                    <p>Prueba con otro nombre o limpia los filtros.</p>
                </div>
            </div>
        </section>
                
    </main>

    <footer class="site-footer">
        <div class="container py-5">
            <div class="row g-4">
                <div class="col-md-6">
                    <a class="brand brand-light" href="<%= ctx %>/IndexSistema">
                        <span class="brand-mark"><i class="bi bi-basket2-fill"></i></span>
                        <span><strong>Minimarket</strong><small>Rosita</small></span>
                    </a>
                    <p class="mt-3">Productos frescos y esenciales, cerca de ti.</p>
                </div>
                <div class="col-md-3">
                    <h3>Navegación</h3>
                    <a href="<%= ctx %>/IndexSistema">Inicio</a>
                    <a href="<%= ctx %>/CatalogoProductos">Productos</a>
                </div>
                <div class="col-md-3">
                    <h3>Tu cuenta</h3>
                    <a href="<%= recursos %>/login.html">Ingresar</a>
                    <a href="<%= recursos %>/registro.html">Registrarte</a>
                </div>
            </div>
            <div class="footer-bottom">
                <span>© <span data-current-year></span> Minimarket Rosita.</span>
                <span>Plantilla base por <a href="https://templatesjungle.com/">TemplatesJungle</a></span>
            </div>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="<%= recursos %>/js/app.js"></script>
    <script>
document.addEventListener("DOMContentLoaded", function () {

    const tarjetas = Array.from(
        document.querySelectorAll(".producto-item")
    );

    const busqueda = document.getElementById("productSearch");
    const categoria = document.getElementById("categoryFilter");
    const orden = document.getElementById("sortProducts");
    const contador = document.getElementById("resultCount");
    const vacio = document.getElementById("emptyProducts");
    const contenedor = document.getElementById("productGrid");

    // Recibir búsqueda desde la página principal
    const parametros = new URLSearchParams(window.location.search);

    busqueda.value = parametros.get("q") || "";

    // Obtener categorías de los productos
    const categorias = [...new Set(
        tarjetas.map(t => t.dataset.categoria).filter(Boolean)
    )];

    categorias.sort().forEach(nombre => {

        const opcion = document.createElement("option");

        opcion.value = nombre;
        opcion.textContent = nombre;

        categoria.appendChild(opcion);
    });

    // Normalizar texto para ignorar mayúsculas y tildes
    function normalizar(texto) {

        return texto.toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .trim();
    }

    // Filtrar productos
    function filtrar() {

        const nombreBuscado = normalizar(busqueda.value);
        const categoriaElegida = categoria.value;

        let visibles = tarjetas.filter(tarjeta => {

            const nombre = normalizar(tarjeta.dataset.nombre);
            const cat = normalizar(tarjeta.dataset.categoria);

            const coincideNombre =
                nombre.includes(nombreBuscado)
                || cat.includes(nombreBuscado);

            const coincideCategoria =
                categoriaElegida === ""
                || tarjeta.dataset.categoria === categoriaElegida;

            const mostrar = coincideNombre && coincideCategoria;

            tarjeta.classList.toggle("d-none", !mostrar);

            return mostrar;
        });

        // Ordenar productos
        visibles.sort((a, b) => {

            if (orden.value === "price-asc") {
                return Number(a.dataset.precio)
                     - Number(b.dataset.precio);
            }

            if (orden.value === "price-desc") {
                return Number(b.dataset.precio)
                     - Number(a.dataset.precio);
            }

            if (orden.value === "name") {
                return a.dataset.nombre.localeCompare(
                    b.dataset.nombre, "es"
                );
            }

            return tarjetas.indexOf(a) - tarjetas.indexOf(b);
        });

        visibles.forEach(tarjeta => {
            contenedor.appendChild(tarjeta);
        });

        // Actualizar cantidad de resultados
        contador.textContent = visibles.length
                + (visibles.length === 1
                   ? " producto" : " productos");

        vacio.classList.toggle("d-none", visibles.length > 0);
    }

    // Eventos de búsqueda
    busqueda.addEventListener("input", filtrar);
    categoria.addEventListener("change", filtrar);
    orden.addEventListener("change", filtrar);

    // Limpiar filtros
    document.getElementById("clearFilters")
        .addEventListener("click", function () {

            busqueda.value = "";
            categoria.value = "";
            orden.value = "default";

            filtrar();
        });

    filtrar();
});
</script>
</body>
</html>
