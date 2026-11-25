package Controlador;

import Modelo.Cliente;
import Modelo.ClienteDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "SVCliente", urlPatterns = {
    "/SVCliente",
    "/RegistroCliente",
    "/RegistrarCliente",
    "/LoginCliente",
    "/IniciarSesionCliente",
    "/CerrarSesionCliente"
})
public class SVCliente extends HttpServlet {

    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String ruta = request.getServletPath();

        ClienteDAO dao = new ClienteDAO();

        HttpSession session = request.getSession();

        // MOSTRAR FORMULARIO DE REGISTRO
        if (ruta.equals("/RegistroCliente")
                || ruta.equals("/SVCliente")) {

            if (session.getAttribute("csrfRegistro") == null) {

                session.setAttribute("csrfRegistro",
                        UUID.randomUUID().toString());
            }

            request.setAttribute("exito",
                    session.getAttribute("registroExitoso"));

            session.removeAttribute("registroExitoso");

            request.getRequestDispatcher(
                "/WEB-INF/vistas/tienda/registro.jsp"
            ).forward(request, response);

            return;
        }

        // REGISTRAR CLIENTE
        if (ruta.equals("/RegistrarCliente")) {

            if (!"POST".equals(request.getMethod())) {
                response.sendError(405);
                return;
            }

            if (session.getAttribute("csrfRegistro") == null
                    || !session.getAttribute("csrfRegistro")
                            .equals(request.getParameter("csrf"))) {

                response.sendError(403);
                return;
            }

            // Recibir datos del formulario
            String nombres = dato(request, "nombres");
            String apellidos = dato(request, "apellidos");

            String correo = dato(request, "correo")
                    .toLowerCase(Locale.ROOT);

            String telefono = dato(request, "telefono");
            String dni = dato(request, "dni");
            String password = request.getParameter("password");

            // Validar datos
            if (nombres.isBlank() || nombres.length() > 100
                    || apellidos.isBlank() || apellidos.length() > 100
                    || correo.length() > 150
                    || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                    || !telefono.matches("[0-9]{9}")
                    || !dni.matches("[0-9]{8}")
                    || password == null
                    || password.length() < 12
                    || password.getBytes(StandardCharsets.UTF_8).length > 72
                    || request.getParameter("terminos") == null) {

                request.setAttribute("error",
                        "Verifica los datos ingresados.");

                request.getRequestDispatcher(
                    "/WEB-INF/vistas/tienda/registro.jsp"
                ).forward(request, response);

                return;
            }

            // Crear objeto cliente
            Cliente cliente = new Cliente();

            cliente.setNombres(nombres);
            cliente.setApellidos(apellidos);
            cliente.setCorreo(correo);
            cliente.setTelefono(telefono);
            cliente.setDni(dni);

            try {

                boolean registrado = dao.registrar(cliente, password);

                if (registrado) {

                    session.removeAttribute("csrfRegistro");

                    session.setAttribute("registroExitoso",
                            "¡Cuenta creada correctamente!");

                    response.sendRedirect(
                        request.getContextPath() + "/RegistroCliente"
                    );

                    return;
                }

                request.setAttribute("error",
                        "No se pudo registrar el cliente.");

            } catch (SQLException e) {

                if ("23505".equals(e.getSQLState())) {

                    request.setAttribute("error",
                            "No se pudo registrar. Verifica tus datos.");

                } else {

                    log("Error al registrar cliente", e);

                    request.setAttribute("error",
                            "Error al registrar el cliente.");
                }
            }

            request.getRequestDispatcher(
                "/WEB-INF/vistas/tienda/registro.jsp"
            ).forward(request, response);

            return;
        }

        // MOSTRAR FORMULARIO DE INICIO DE SESIÓN
        if (ruta.equals("/LoginCliente")) {

            // Si ya inició sesión, regresar al inicio
            if (session.getAttribute("clienteLogueado") != null) {

                response.sendRedirect(
                    request.getContextPath() + "/IndexSistema"
                );

                return;
            }

            // Generar token de seguridad
            if (session.getAttribute("csrfLogin") == null) {

                session.setAttribute("csrfLogin",
                        UUID.randomUUID().toString());
            }

            request.getRequestDispatcher(
                "/WEB-INF/vistas/tienda/loginCliente.jsp"
            ).forward(request, response);

            return;
        }

        // INICIAR SESIÓN
        if (ruta.equals("/IniciarSesionCliente")) {

            if (!"POST".equals(request.getMethod())) {
                response.sendError(405);
                return;
            }

            if (session.getAttribute("csrfLogin") == null
                    || !session.getAttribute("csrfLogin")
                            .equals(request.getParameter("csrf"))) {

                response.sendError(403);
                return;
            }

            String correo = dato(request, "correo")
                    .toLowerCase(Locale.ROOT);

            String password = request.getParameter("password");

            try {

                Cliente cliente = null;

                // Validar y consultar credenciales
                if (!correo.isBlank()
                        && correo.length() <= 150
                        && password != null
                        && password.getBytes(StandardCharsets.UTF_8).length <= 72) {

                    cliente = dao.iniciarSesion(correo, password);
                }

                // Verificar si existe el cliente
                if (cliente != null) {

                    // Renovar identificador de sesión
                    request.changeSessionId();

                    // Guardar el cliente en la sesión
                    session.setAttribute("clienteLogueado", cliente);

                    session.setAttribute("csrfLogin",
                            UUID.randomUUID().toString());

                    // Regresar a la página principal
                    response.sendRedirect(
                        request.getContextPath() + "/IndexSistema"
                    );

                    return;
                }

                request.setAttribute("error",
                        "Correo o contraseña incorrectos.");

            } catch (SQLException e) {

                log("Error al iniciar sesión", e);

                request.setAttribute("error",
                        "No se pudo iniciar sesión.");
            }

            request.getRequestDispatcher(
                "/WEB-INF/vistas/tienda/loginCliente.jsp"
            ).forward(request, response);

            return;
        }

        // CERRAR SESIÓN
        if (ruta.equals("/CerrarSesionCliente")) {

            if (!"POST".equals(request.getMethod())) {
                response.sendError(405);
                return;
            }

            if (session.getAttribute("csrfLogin") == null
                    || !session.getAttribute("csrfLogin")
                            .equals(request.getParameter("csrf"))) {

                response.sendError(403);
                return;
            }

            // Eliminar información de sesión del cliente
          session.invalidate();
            request.changeSessionId();

            response.sendRedirect(
                request.getContextPath() + "/IndexSistema"
            );

            return;
        }

        response.sendError(404);
    }

    // Obtener parámetros evitando valores nulos
    private String dato(HttpServletRequest request, String campo) {

        String valor = request.getParameter(campo);

        return valor == null ? "" : valor.trim();
    }

    // Método GET
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(request, response);
    }

    // Método POST
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Servlet de clientes de Minimarket Rosita";
    }
}
