
package Controlador;

import Modelo.Carrito;
import Modelo.CarritoDAO;
import Modelo.ReservaDAO;
import Modelo.Tienda;
import Modelo.TiendaDAO;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import Modelo.Pedido;
import Modelo.PedidoDAO;
import Modelo.PedidoDetalle;
import java.util.ArrayList;
import Modelo.Cliente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "SVCarrito", urlPatterns = {
    "/SVCarrito",
    "/Carrito",
    "/AgregarCarrito",
    "/ActualizarCarrito",
    "/EliminarCarrito",
    "/VaciarCarrito",
        "/RegistrarPedidoCarrito",
        "/MisPedidos",
        "/HistorialPedidos",
        "/DetallePedidoCliente"
})
public class SVCarrito extends HttpServlet {

    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // Instanciamos el DAO
        CarritoDAO daoCarrito = new CarritoDAO();
        
        // Identificamos la ruta solicitada
        String ruta = request.getServletPath();

        // Obtenemos la sesión del cliente
        HttpSession session = request.getSession();
        synchronized (session) {
        ReservaDAO reservas = new ReservaDAO();
        UUID reservaId = ReservaDAO.id(session);
        
        Cliente cliente = (Cliente)
        session.getAttribute("clienteLogueado");
        // Recuperamos el carrito guardado en sesión
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> carrito =
                (Map<Integer, Integer>)
                session.getAttribute("carrito");

        // Si todavía no existe, creamos uno vacío
        if (carrito == null) {

            carrito = new LinkedHashMap<>();

            session.setAttribute("carrito", carrito);
        }

        try {
            carrito = reservas.cargar(reservaId);
            session.setAttribute("carrito", carrito);

            // MOSTRAR CARRITO
            if (ruta.equals("/Carrito")
                    || ruta.equals("/SVCarrito") || ruta.equals("/MisPedidos")) {

                if (!request.getMethod().equals("GET")) {
                    response.sendError(405);
                    return;
                }

                // Recuperamos productos desde Supabase
                List<Carrito> lista =
                        daoCarrito.listarProductos(carrito);

                // Calculamos el subtotal
                double subtotal =
                        daoCarrito.calcularSubtotal(lista);

                // Calculamos la cantidad de unidades
                int cantidadProductos = 0;

                for (Carrito item : lista) {
                    cantidadProductos += item.getCantidad();
                }

                // Enviamos datos al JSP
                request.setAttribute("productosCarrito", lista);
                request.setAttribute("subtotal", subtotal);
                request.setAttribute("reservaVence", reservas.vence(reservaId));
                request.setAttribute(
                        "cantidadProductos",
                        cantidadProductos
                );
                if (session.getAttribute("csrfCarrito") == null) {

    session.setAttribute("csrfCarrito",
            UUID.randomUUID().toString());
}

                // Mostramos la vista
                request.getRequestDispatcher(
                        "/WEB-INF/vistas/tienda/carrito.jsp"
                ).forward(request, response);

            }
            //Mostrar la lista de pedidos de un cliente
            else if(ruta.equals("/MisPedidos")){
                request.getRequestDispatcher("/WEB-INF/vistas/tienda/carrito.jsp").forward(request, response);
                
            }
            else if (ruta.equals("/HistorialPedidos")) {

    if (!"GET".equals(request.getMethod())) {
        response.sendError(405);
        return;
    }

    if (cliente == null) {

        response.sendRedirect(
            request.getContextPath() + "/LoginCliente"
        );

        return;
    }

    PedidoDAO dao = new PedidoDAO();
//Recuperar todos los pedidos del cliente en especifico
    List<Pedido> pedidos =
            dao.listarPorCliente(cliente.getId());

    request.setAttribute("pedidos", pedidos);

    request.getRequestDispatcher(
        "/WEB-INF/vistas/tienda/misPedidos.jsp"
    ).forward(request, response);

    return;
}
            
  else if (ruta.equals("/DetallePedidoCliente")) {

    if (!"GET".equals(request.getMethod())) {
        response.sendError(405);
        return;
    }

    // Verificar inicio de sesión
    if (cliente == null) {

        response.sendRedirect(
            request.getContextPath() + "/LoginCliente"
        );

        return;
    }

    // Obtener el ID del pedido seleccionado
    String id = request.getParameter("id");

    if (id == null) {
        response.sendError(400);
        return;
    }

    try {
        UUID.fromString(id);
    } catch (IllegalArgumentException e) {
        response.sendError(400);
        return;
    }

    PedidoDAO dao = new PedidoDAO();

    // Buscar el pedido del cliente
    Pedido pedido = dao.buscarPorCliente(
            id, cliente.getId());

    if (pedido == null) {

        response.sendError(404, "Pedido no encontrado");
        return;
    }

    // Recuperar productos del pedido
    List<PedidoDetalle> detalles =
            dao.listarDetallesCliente(id, cliente.getId());

    // Enviar información al JSP
    request.setAttribute("pedido", pedido);
    request.setAttribute("detalles", detalles);

    response.setHeader("Cache-Control", "no-store");

    request.getRequestDispatcher(
        "/WEB-INF/vistas/tienda/detallePedidoCliente.jsp"
    ).forward(request, response);

    return;
}          

            // AGREGAR PRODUCTO
            else if (ruta.equals("/AgregarCarrito")) {

                if (!request.getMethod().equals("POST")) {
                    response.sendError(405);
                    return;
                }

                // Recibimos los datos del formulario
                int productoId = Integer.parseInt(
                        request.getParameter("productoId")
                );

                int cantidad = Integer.parseInt(
                        request.getParameter("cantidad")
                );

                if (productoId <= 0 || cantidad <= 0) {
                    response.sendError(
                            400,
                            "Producto o cantidad inválida"
                    );
                    return;
                }

                // Cantidad que ya tenía el carrito
                int cantidadActual =
                        carrito.getOrDefault(productoId, 0);

                long nuevaCantidad =
                        (long) cantidadActual + cantidad;

                // Verificamos stock en Supabase
                if (nuevaCantidad > Integer.MAX_VALUE
                        || !reservas.reservar(reservaId, productoId, (int) nuevaCantidad)) {

                    response.sendError(
                            400,
                            "Stock insuficiente o producto no disponible"
                    );
                    return;
                }

                // Guardamos el producto en sesión
                carrito.put(productoId, (int) nuevaCantidad);

              // Volvemos al catálogo para que el cliente siga comprando
response.sendRedirect(request.getContextPath() + "/ListaProductos");
return;
            }
else if (ruta.equals("/RegistrarPedidoCarrito")) {

    if (!"POST".equals(request.getMethod())) {
        response.sendError(405);
        return;
    }

    // Validar token de seguridad
    if (session.getAttribute("csrfCarrito") == null
            || !session.getAttribute("csrfCarrito")
                    .equals(request.getParameter("csrf"))) {

        response.sendError(403);
        return;
    }

    String pedidoId;

    synchronized (carrito) {

        // Verificar que existan productos
        if (carrito.isEmpty()) {

            response.sendRedirect(
                request.getContextPath() + "/Carrito"
            );

            return;
        }

        // Crear el pedido
        Pedido pedido = new Pedido();

        // Asociar cuenta solamente si inició sesión
        if (cliente != null) {
            pedido.setCuentaClienteId(cliente.getId());
        }

        // Recibir datos del formulario
        pedido.setNombreContacto(
                request.getParameter("nombre_contacto"));

        pedido.setTelefonoContacto(
                request.getParameter("telefono_contacto"));

        pedido.setTipoEntrega(
                request.getParameter("tipo_entrega"));

        pedido.setMetodoPago(
                request.getParameter("metodo_pago"));

        pedido.setObservaciones(
                request.getParameter("observaciones"));

        // Dirección solamente para delivery
        if ("DELIVERY".equals(pedido.getTipoEntrega())) {

            pedido.setDireccionEntrega(
                    request.getParameter("direccion_entrega"));
        }

        // Preparar los productos del pedido
        List<PedidoDetalle> detalles = new ArrayList<>();

        for (Map.Entry<Integer, Integer> item : carrito.entrySet()) {

            PedidoDetalle detalle = new PedidoDetalle();

            detalle.setProductoId(item.getKey().longValue());
            detalle.setCantidad(item.getValue());

            detalles.add(detalle);
        }

        // Guardar pedido en PostgreSQL
        new PedidoDAO().agregarReservado(pedido, detalles, reservaId);

        pedidoId = pedido.getId();

        // Limpiar carrito después de registrar
        carrito.clear();

        session.setAttribute("pedidoRegistrado", pedidoId);
        session.removeAttribute("csrfCarrito");
    }

    // Enviar pedido por WhatsApp
    abrirWhatsAppPedido(request, response, pedidoId);

    return;
}     // ACTUALIZAR CANTIDAD
            else if (ruta.equals("/ActualizarCarrito")) {

                if (!request.getMethod().equals("POST")) {
                    response.sendError(405);
                    return;
                }

                int productoId = Integer.parseInt(
                        request.getParameter("productoId")
                );

                int cantidad = Integer.parseInt(
                        request.getParameter("cantidad")
                );

                if (!carrito.containsKey(productoId)) {
                    response.sendError(
                            400,
                            "El producto no existe en el carrito"
                    );
                    return;
                }

                if (cantidad <= 0) {

                    // Si la cantidad es cero, eliminamos
                    reservas.reservar(reservaId, productoId, 0);
                    carrito.remove(productoId);

                } else {

                    // Validamos stock disponible
                    if (!reservas.reservar(reservaId, productoId, cantidad)) {

                        response.sendError(
                                400,
                                "Stock insuficiente"
                        );
                        return;
                    }

                    carrito.put(productoId, cantidad);
                }

                response.sendRedirect(
                        request.getContextPath() + "/Carrito"
                );

            }

            // ELIMINAR PRODUCTO
            else if (ruta.equals("/EliminarCarrito")) {

                if (!request.getMethod().equals("POST")) {
                    response.sendError(405);
                    return;
                }

                int productoId = Integer.parseInt(
                        request.getParameter("productoId")
                );

                reservas.reservar(reservaId, productoId, 0);
                carrito.remove(productoId);

                response.sendRedirect(
                        request.getContextPath() + "/Carrito"
                );

            }

            // VACIAR CARRITO
            else if (ruta.equals("/VaciarCarrito")) {

                if (!request.getMethod().equals("POST")) {
                    response.sendError(405);
                    return;
                }

                reservas.liberar(reservaId);
                carrito.clear();

                response.sendRedirect(
                        request.getContextPath() + "/Carrito"
                );

            } else {

                response.sendError(
                        404,
                        "Ruta del carrito no encontrada"
                );
            }

        } catch (NumberFormatException e) {

            response.sendError(
                    400,
                    "El ID del producto o la cantidad no son válidos"
            );

        } catch (IllegalArgumentException e) {

            response.sendError(
                    400,
                    e.getMessage()
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Error al consultar el carrito en Supabase",
                    e
            );
        }
    }
    }
    private void abrirWhatsAppPedido(
        HttpServletRequest request,
        HttpServletResponse response,
        String pedidoId) throws IOException {

    String destino = request.getContextPath() + "/Carrito";

    try {
        Tienda tienda = new TiendaDAO().mostrarInfo();
        String whatsapp = tienda == null ? null : tienda.getWhatsapp();
        String numero = whatsapp == null ? "" : whatsapp.replaceAll("[^0-9]", "");

        // Completa el código de Perú si se guardó un celular de 9 dígitos.
        if (numero.matches("9[0-9]{8}")) {
            numero = "51" + numero;
        }

        if (numero.matches("[1-9][0-9]{7,14}")) {
            PedidoDAO dao = new PedidoDAO();
            Pedido pedido = dao.buscar(pedidoId);

            if (pedido != null) {
                String referencia = pedido.getNumero();
                if (referencia == null || referencia.isBlank()) {
                    referencia = pedidoId;
                }

                StringBuilder mensaje = new StringBuilder("Hola, Minimarket Rosita.")
                        .append("\nPedido: ").append(referencia)
                        .append("\nCliente: ").append(pedido.getNombreContacto())
                        .append("\nCelular: ").append(pedido.getTelefonoContacto())
                        .append("\nEntrega: ").append(pedido.getTipoEntrega());

                if ("DELIVERY".equals(pedido.getTipoEntrega())) {
                    mensaje.append("\nDirección: ").append(pedido.getDireccionEntrega());
                }

                mensaje.append("\nPago: ").append(pedido.getMetodoPago())
                       .append("\n\nProductos:");

                for (PedidoDetalle detalle : dao.listarDetalles(pedidoId)) {
                    mensaje.append("\n- ").append(detalle.getCantidad())
                           .append(" × ").append(detalle.getNombreProducto());
                }

                mensaje.append("\n\nTotal estimado: S/ ")
                       .append(String.format(java.util.Locale.US, "%.2f",
                               pedido.getTotalEstimado()));

                String texto = URLEncoder.encode(
                        mensaje.toString(), StandardCharsets.UTF_8)
                        .replace("+", "%20");

                destino = "https://wa.me/" + numero + "?text=" + texto;
            }
        }
    } catch (SQLException e) {
        log("Pedido guardado, pero no se pudo preparar WhatsApp: " + pedidoId, e);
    }

    response.sendRedirect(destino);
}

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Controlador del carrito de compras de Minimarket Rosita";
    }
}
