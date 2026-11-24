<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="es">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Crear cuenta</title>

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
            Tu minimarket,<br>
            más cerca de ti
        </h1>

        <p>
            Crea tu cuenta y descubre promociones
            en todo lo que te gusta.
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
                NUEVO CLIENTE
            </div>

            <h2>Crear cuenta</h2>

            <p class="descripcion">
                Completa tus datos para realizar tus pedidos.
            </p>

            <% if (request.getAttribute("error") != null) { %>

                <div class="alert alert-danger">
                    ${requestScope.error}
                </div>

            <% } %>

            <% if (request.getAttribute("exito") != null) { %>

                <div class="alert alert-success">
                    ${requestScope.exito}
                </div>

            <% } %>

            <form action="${pageContext.request.contextPath}/RegistrarCliente"
                  method="POST">

                <input type="hidden"
                       name="csrf"
                       value="${sessionScope.csrfRegistro}">

                <div class="row g-3">

                    <div class="col-md-6">

                        <label class="form-label" for="nombres">
                            Nombres
                        </label>

                        <input type="text"
                               id="nombres"
                               name="nombres"
                               class="form-control"
                               maxlength="100"
                               required>

                    </div>

                    <div class="col-md-6">

                        <label class="form-label" for="apellidos">
                            Apellidos
                        </label>

                        <input type="text"
                               id="apellidos"
                               name="apellidos"
                               class="form-control"
                               maxlength="100"
                               required>

                    </div>

                    <div class="col-12">

                        <label class="form-label" for="correo">
                            Correo electrónico
                        </label>

                        <input type="email"
                               id="correo"
                               name="correo"
                               class="form-control"
                               placeholder="nombre@correo.com"
                               maxlength="150"
                               required>

                    </div>

                    <div class="col-md-6">

                        <label class="form-label" for="telefono">
                            Celular
                        </label>

                        <input type="tel"
                               id="telefono"
                               name="telefono"
                               class="form-control"
                               placeholder="987654321"
                               pattern="[0-9]{9}"
                               maxlength="9"
                               required>

                    </div>

                    <div class="col-md-6">

                        <label class="form-label" for="dni">
                            DNI
                        </label>

                        <input type="text"
                               id="dni"
                               name="dni"
                               class="form-control"
                               placeholder="12345678"
                               inputmode="numeric"
                               pattern="[0-9]{8}"
                               maxlength="8"
                               required>

                    </div>

                    <div class="col-12">

                        <label class="form-label" for="password">
                            Contraseña
                        </label>

                        <div class="password-box">

                            <input type="password"
                                   id="password"
                                   name="password"
                                   class="form-control"
                                   minlength="12"
                                   maxlength="72"
                                   autocomplete="new-password"
                                   required>

                            <button type="button"
                                    aria-label="Mostrar contraseña"
                                    onclick="mostrarPassword()">

                                <i id="iconoPassword"
                                   class="bi bi-eye"></i>

                            </button>

                        </div>

                        <small class="text-secondary">
                            Mínimo 12 caracteres.
                        </small>

                    </div>

                    <div class="col-12">

                        <div class="form-check">

                            <input type="checkbox"
                                   class="form-check-input"
                                   id="terminos"
                                   name="terminos"
                                   required>

                            <label class="form-check-label"
                                   for="terminos">

                                Acepto los términos y la
                                política de privacidad.

                            </label>

                        </div>

                    </div>

                    <div class="col-12">

                        <button type="submit"
                                class="btn btn-registro w-100">

                            Crear mi cuenta

                        </button>

                    </div>

                </div>

            </form>

            <p class="iniciar-sesion">

                ¿Ya tienes cuenta?

                <a href="${pageContext.request.contextPath}/LoginCliente">
                    Inicia sesión
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