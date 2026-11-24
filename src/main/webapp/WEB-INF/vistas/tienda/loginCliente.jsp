<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="es">

<head>

    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Iniciar sesión | Minimarket Rosita</title>

    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">

    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/sistema_pedidos/estilosPedidos/registro.css">
    <link rel="stylesheet"
      href="${pageContext.request.contextPath}/sistema_pedidos/estilosPedidos/tema.css">

<script src="${pageContext.request.contextPath}/sistema_pedidos/js/tema.js"></script>
</head>

<body>

<div class="registro-layout">

    <section class="panel-imagen"
             style="background-image:
             linear-gradient(rgba(10,30,15,.25), rgba(10,40,20,.90)),
             url('${pageContext.request.contextPath}/sistema_pedidos/images/hero-minimarket-rosita.png');">

        <div class="contenido-imagen">

            <div class="marca">
                <i class="bi bi-basket-fill"></i>
                MINIMARKET ROSITA
            </div>

            <h1>
                ¡Bienvenido<br>
                de nuevo!
            </h1>

            <p>
                Realiza tus compras y consulta tus pedidos.
            </p>

        </div>

    </section>

    <section class="panel-formulario">

        <div class="formulario">

            <a class="volver"
               href="${pageContext.request.contextPath}/IndexSistema">
                ← Volver al inicio
            </a>

            <div class="nuevo-cliente">
                BIENVENIDO CLIENTE
            </div>

            <h2>Iniciar sesión</h2>

            <p class="descripcion">
                Ingresa tus datos para acceder a tu cuenta.
            </p>

            <% if (request.getAttribute("error") != null) { %>

                <div class="alert alert-danger">
                    ${requestScope.error}
                </div>

            <% } %>

            <form action="${pageContext.request.contextPath}/IniciarSesionCliente"
                  method="POST">

                <input type="hidden"
                       name="csrf"
                       value="${sessionScope.csrfLogin}">

                <div class="mb-3">

                    <label class="form-label" for="correo">
                        Correo electrónico
                    </label>

                    <input type="email"
                           id="correo"
                           name="correo"
                           class="form-control"
                           placeholder="nombre@correo.com"
                           autocomplete="username"
                           required>

                </div>

                <div class="mb-4">

                    <label class="form-label" for="password">
                        Contraseña
                    </label>

                    <div class="password-box">

                        <input type="password"
                               id="password"
                               name="password"
                               class="form-control"
                               autocomplete="current-password"
                               required>

                        <button type="button"
                                aria-label="Mostrar contraseña"
                                onclick="mostrarPassword()">

                            <i id="iconoPassword"
                               class="bi bi-eye"></i>

                        </button>

                    </div>

                </div>

                <button type="submit"
                        class="btn btn-registro w-100">

                    Iniciar sesión

                </button>

            </form>

            <p class="iniciar-sesion">

                ¿No tienes cuenta?

                <a href="${pageContext.request.contextPath}/RegistroCliente">
                    Crear cuenta
                </a>

            </p>

        </div>

    </section>

</div>

<script>

function mostrarPassword() {

    const password = document.getElementById("password");
    const icono = document.getElementById("iconoPassword");

    if (password.type === "password") {

        password.type = "text";
        icono.className = "bi bi-eye-slash";

    } else {

        password.type = "password";
        icono.className = "bi bi-eye";

    }
}

</script>

</body>
</html>