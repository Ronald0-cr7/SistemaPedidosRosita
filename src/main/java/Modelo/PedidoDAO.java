
package Modelo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PedidoDAO {

    // READ: Listar todos los pedidos
    public List<Pedido> listar() throws SQLException {

        List<Pedido> lista = new ArrayList<>();

        String sql = "SELECT * FROM public.pedidos "
                   + "ORDER BY creado_en DESC";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(convertirPedido(rs));
            }
        }

        return lista;
    }
    
    // ==========================================
    // READ: FILTRAR PEDIDOS POR FECHAS
    // HORARIO DE PERÚ
    // ==========================================

    public List<Pedido> listarPorFechas(
            java.time.LocalDate desde,
            java.time.LocalDate hasta) throws SQLException {

        // Validar fechas
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException(
                "Debe seleccionar ambas fechas");
        }

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                "La fecha inicial no puede ser mayor que la final");
        }

        // Lista donde se almacenarán los pedidos
        List<Pedido> lista = new ArrayList<>();

        // Consulta SQL con filtro por fechas
        String sql = "SELECT * FROM public.pedidos "
                + "WHERE (creado_en AT TIME ZONE 'America/Lima')::date "
                + "BETWEEN ? AND ? "
                + "ORDER BY creado_en DESC";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignar fechas a la consulta
            ps.setObject(1, desde);
            ps.setObject(2, hasta);

            // Ejecutar consulta
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    // Convertir cada registro a objeto Pedido
                    Pedido p = convertirPedido(rs);

                    // Agregar pedido a la lista
                    lista.add(p);
                }
            }
        }

        return lista;
    }


    // READ: Buscar pedido por ID
    public Pedido buscar(String id) throws SQLException {

        String sql = "SELECT * FROM public.pedidos "
                   + "WHERE id = ?::uuid";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirPedido(rs);
                }
            }
        }

        return null;
    }

    // Convertir los datos SQL a un objeto Pedido
    private Pedido convertirPedido(ResultSet rs)
            throws SQLException {

        Pedido p = new Pedido();

        p.setId(rs.getString("id"));
        p.setCuentaClienteId(rs.getString("cuenta_cliente_id"));
        p.setNumero(rs.getString("numero"));
        p.setNombreContacto(rs.getString("nombre_contacto"));
        p.setTelefonoContacto(rs.getString("telefono_contacto"));
        p.setTipoEntrega(rs.getString("tipo_entrega"));
        p.setDireccionEntrega(rs.getString("direccion_entrega"));
        p.setMetodoPago(rs.getString("metodo_pago"));
        p.setEstado(rs.getString("estado"));
        p.setObservaciones(rs.getString("observaciones"));
        p.setCostoDelibery(rs.getDouble("costo_delivery"));
        p.setSubtotal(rs.getDouble("subtotal"));
        p.setTotalEstimado(rs.getDouble("total_estimado"));
        p.setCreadoEn(rs.getString("creado_en"));

      // =====================================
        // FECHA Y HORA EN HORARIO DE PERÚ
        // Formato: dd/MM/yyyy HH:mm:ss
        // =====================================

        java.time.OffsetDateTime fechaUTC =
                rs.getObject(
                    "creado_en",
                    java.time.OffsetDateTime.class
                );

        if (fechaUTC != null) {

            java.time.ZonedDateTime fechaPeru =
                    fechaUTC.atZoneSameInstant(
                        java.time.ZoneId.of("America/Lima")
                    );

            java.time.format.DateTimeFormatter formato =
                    java.time.format.DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy HH:mm:ss"
                    );

            p.setCreadoEn(fechaPeru.format(formato));

        } else {

            p.setCreadoEn("");

        }

        return p;
    }

    // READ: Listar productos de un pedido
    public List<PedidoDetalle> listarDetalles(String pedidoId)
            throws SQLException {

        List<PedidoDetalle> lista = new ArrayList<>();

        String sql = "SELECT * FROM public.pedido_detalles "
                   + "WHERE pedido_id = ?::uuid ORDER BY id";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pedidoId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    PedidoDetalle d = new PedidoDetalle();

                    d.setId((int) rs.getLong("id"));
                    d.setPedidoId(rs.getString("pedido_id"));
                    d.setProductoId((int) rs.getLong("producto_id"));
                    d.setNombreProducto(
                        rs.getString("nombre_producto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(
                        rs.getDouble("precio_unitario"));
                    d.setSubtotal(rs.getDouble("subtotal"));

                    lista.add(d);
                }
            }
        }

        return lista;
    }

    // Validar y agrupar los productos
    private Map<Long, Integer> agrupar(
            List<PedidoDetalle> detalles) throws SQLException {

        if (detalles == null || detalles.isEmpty()) {
            throw new SQLException("El pedido debe tener productos");
        }

        Map<Long, Integer> cantidades = new TreeMap<>();

        for (PedidoDetalle d : detalles) {

            if (d.getProductoId() <= 0 || d.getCantidad() <= 0) {
                throw new SQLException("Producto o cantidad inválida");
            }

            try {
                cantidades.merge(
                    d.getProductoId(),
                    d.getCantidad(),
                    Math::addExact
                );
            } catch (ArithmeticException e) {
                throw new SQLException("Cantidad demasiado grande", e);
            }
        }

        return cantidades;
    }

    // Validar los datos generales
    private void validar(Pedido p) throws SQLException {

        if (p == null ||
            p.getNombreContacto() == null ||
            p.getNombreContacto().isBlank() ||
            p.getTelefonoContacto() == null ||
            !p.getTelefonoContacto().matches("[0-9]{9}")) {

            throw new SQLException("Datos del cliente inválidos");
        }

        if (!"RECOJO".equals(p.getTipoEntrega()) &&
            !"DELIVERY".equals(p.getTipoEntrega())) {
            throw new SQLException("Tipo de entrega inválido");
        }

        if ("DELIVERY".equals(p.getTipoEntrega()) &&
            (p.getDireccionEntrega() == null ||
             p.getDireccionEntrega().isBlank())) {
            throw new SQLException("Falta dirección de entrega");
        }

        if (!java.util.Arrays.asList(
            "EFECTIVO", "YAPE", "PLIN", "TARJETA", "OTRO"
        ).contains(p.getMetodoPago())) {
            throw new SQLException("Método de pago inválido");
        }
    }

    // Obtener el costo de delivery desde la tienda
    private BigDecimal obtenerDelivery(
            Connection con, Pedido p) throws SQLException {

        if ("RECOJO".equals(p.getTipoEntrega())) {
            return BigDecimal.ZERO;
        }

        String sql = "SELECT costo_delivery "
                   + "FROM public.tienda WHERE id = 1";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                throw new SQLException("Configuración de tienda no encontrada");
            }

            return rs.getBigDecimal("costo_delivery");
        }
    }

    // Consultar precios y registrar los detalles
    private BigDecimal guardarDetalles(
            Connection con,
            String pedidoId,
            Map<Long, Integer> cantidades) throws SQLException {

        String sqlProducto = "SELECT nombre, precio, stock "
                + "FROM public.productos "
                + "WHERE id = ? AND activo = true "
                + "AND eliminado = false FOR UPDATE";

        String sqlDetalle = "INSERT INTO public.pedido_detalles "
                + "(pedido_id, producto_id, nombre_producto, "
                + "cantidad, precio_unitario) "
                + "VALUES (?::uuid, ?, ?, ?, ?)";

        BigDecimal total = BigDecimal.ZERO;

        try (PreparedStatement psProducto =
                    con.prepareStatement(sqlProducto);
             PreparedStatement psDetalle =
                    con.prepareStatement(sqlDetalle)) {

            for (Map.Entry<Long, Integer> item :
                    cantidades.entrySet()) {

                long productoId = item.getKey();
                int cantidad = item.getValue();

                psProducto.setLong(1, productoId);

                try (ResultSet rs = psProducto.executeQuery()) {

                    if (!rs.next()) {
                        throw new SQLException(
                            "Producto no disponible: " + productoId);
                    }

                    if (rs.getInt("stock") < cantidad) {
                        throw new SQLException(
                            "Stock insuficiente: " + productoId);
                    }

                    String nombre = rs.getString("nombre");
                    BigDecimal precio = rs.getBigDecimal("precio");

                    BigDecimal subtotal = precio.multiply(
                        BigDecimal.valueOf(cantidad));

                    total = total.add(subtotal);

                    psDetalle.setString(1, pedidoId);
                    psDetalle.setLong(2, productoId);
                    psDetalle.setString(3, nombre);
                    psDetalle.setInt(4, cantidad);
                    psDetalle.setBigDecimal(5, precio);

                    psDetalle.executeUpdate();
                }
            }
        }

        return total;
    }

    // CREATE: Agregar pedido con sus productos
    public void agregar(Pedido p, List<PedidoDetalle> detalles)
            throws SQLException {

        validar(p);
        Map<Long, Integer> cantidades = agrupar(detalles);

  String sql = "INSERT INTO public.pedidos "
        + "(nombre_contacto, telefono_contacto, "
        + "tipo_entrega, direccion_entrega, "
        + "metodo_pago, observaciones, "
        + "costo_delivery, subtotal, cuenta_cliente_id) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?::uuid) "
        + "RETURNING id";
        try (Connection con = Conexion.conectar()) {

            con.setAutoCommit(false);

            try {
                BigDecimal delivery = obtenerDelivery(con, p);
                String pedidoId;

                // Registrar cabecera
                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {

                    ps.setString(1, p.getNombreContacto());
                    ps.setString(2, p.getTelefonoContacto());
                    ps.setString(3, p.getTipoEntrega());
                    ps.setString(4, p.getDireccionEntrega());
                    ps.setString(5, p.getMetodoPago());
                    ps.setString(6, p.getObservaciones());
                    ps.setBigDecimal(7, delivery);
                    ps.setString(8, p.getCuentaClienteId());

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        pedidoId = rs.getString("id");
                    }
                }

                // Registrar productos y calcular subtotal
                BigDecimal subtotal =
                    guardarDetalles(con, pedidoId, cantidades);

                // Actualizar importe del pedido
                try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE public.pedidos SET subtotal = ? "
                    + "WHERE id = ?::uuid")) {

                    ps.setBigDecimal(1, subtotal);
                    ps.setString(2, pedidoId);
                    ps.executeUpdate();
                }

                con.commit();
                p.setId(pedidoId);

            } catch (Exception e) {
                con.rollback();
                throw new SQLException(
                    "No se pudo registrar el pedido", e);
            }
        }
    }

    // UPDATE: Actualizar pedido y sus productos
    public void actualizar(Pedido p, List<PedidoDetalle> detalles)
            throws SQLException {

        validar(p);
        Map<Long, Integer> cantidades = agrupar(detalles);

        if (p.getId() == null || p.getId().isBlank()) {
            throw new SQLException("Falta el ID del pedido");
        }

        try (Connection con = Conexion.conectar()) {

            con.setAutoCommit(false);

            try {
                // Bloquear pedido y verificar estado
                try (PreparedStatement ps = con.prepareStatement(
                    "SELECT estado FROM public.pedidos "
                    + "WHERE id = ?::uuid FOR UPDATE")) {

                    ps.setString(1, p.getId());

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next() ||
                            !"Pendiente".equals(rs.getString("estado"))) {
                            throw new SQLException(
                                "Solo se editan pedidos pendientes");
                        }
                    }
                }

                // Eliminar detalles anteriores
                try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM public.pedido_detalles "
                    + "WHERE pedido_id = ?::uuid")) {

                    ps.setString(1, p.getId());
                    ps.executeUpdate();
                }

                // Insertar nuevos detalles
                BigDecimal subtotal =
                    guardarDetalles(con, p.getId(), cantidades);

                BigDecimal delivery = obtenerDelivery(con, p);

                String sql = "UPDATE public.pedidos SET "
                        + "nombre_contacto = ?, "
                        + "telefono_contacto = ?, "
                        + "tipo_entrega = ?, "
                        + "direccion_entrega = ?, "
                        + "metodo_pago = ?, "
                        + "observaciones = ?, "
                        + "costo_delivery = ?, "
                        + "subtotal = ? "
                        + "WHERE id = ?::uuid";

                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {

                    ps.setString(1, p.getNombreContacto());
                    ps.setString(2, p.getTelefonoContacto());
                    ps.setString(3, p.getTipoEntrega());
                    ps.setString(4, p.getDireccionEntrega());
                    ps.setString(5, p.getMetodoPago());
                    ps.setString(6, p.getObservaciones());
                    ps.setBigDecimal(7, delivery);
                    ps.setBigDecimal(8, subtotal);
                    ps.setString(9, p.getId());

                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw new SQLException(
                    "No se pudo actualizar el pedido", e);
            }
        }
    }

    // DELETE: Eliminar pedido y sus detalles
    public boolean eliminar(String pedidoId) throws SQLException {

        try (Connection con = Conexion.conectar()) {

            con.setAutoCommit(false);

            try {
                // Solo eliminar pedidos pendientes
                try (PreparedStatement ps = con.prepareStatement(
                    "SELECT estado FROM public.pedidos "
                    + "WHERE id = ?::uuid FOR UPDATE")) {

                    ps.setString(1, pedidoId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next() ||
                            !"Pendiente".equals(rs.getString("estado"))) {
                            throw new SQLException(
                                "Solo se eliminan pedidos pendientes");
                        }
                    }
                }

                // Eliminar detalles
                try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM public.pedido_detalles "
                    + "WHERE pedido_id = ?::uuid")) {

                    ps.setString(1, pedidoId);
                    ps.executeUpdate();
                }

                // Eliminar cabecera
                int filas;

                try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM public.pedidos "
                    + "WHERE id = ?::uuid")) {

                    ps.setString(1, pedidoId);
                    filas = ps.executeUpdate();
                }

                con.commit();
                return filas == 1;

            } catch (Exception e) {
                con.rollback();
                throw new SQLException(
                    "No se pudo eliminar el pedido", e);
            }
        }
    }

    // UPDATE: Cambiar estado del pedido
    public boolean cambiarEstado(String id, String estado)
            throws SQLException {

        if (!"Confirmado".equals(estado) &&
            !"Cancelado".equals(estado)) {
            throw new SQLException("Estado no permitido en este método");
        }

        String sql = "UPDATE public.pedidos SET estado = ? "
                + "WHERE id = ?::uuid AND ("
                + "(estado = 'Pendiente' AND ? = 'Confirmado') "
                + "OR (estado IN ('Pendiente','Confirmado') "
                + "AND ? = 'Cancelado'))";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setString(2, id);
            ps.setString(3, estado);
            ps.setString(4, estado);

            return ps.executeUpdate() == 1;
        }
    }

 // UPDATE: Marcar un pedido como atendido
public boolean atenderPedido(String id) throws SQLException {

    String sql = "UPDATE public.pedidos "
               + "SET estado = 'Atendido' "
               + "WHERE id = ?::uuid "
               + "AND estado = 'Confirmado'";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, id);

        return ps.executeUpdate() == 1;

    } catch (SQLException e) {
        throw new SQLException(
            "Error al atender el pedido", e
        );
    }
}

//Filtrar los pedidos de un cliente en especifico
public List<Pedido> listarPorCliente(String clienteId)
        throws SQLException {

    List<Pedido> lista = new ArrayList<>();

    String sql = "SELECT * FROM public.pedidos "
            + "WHERE cuenta_cliente_id = ?::uuid "
            + "ORDER BY creado_en DESC";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, clienteId);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                lista.add(convertirPedido(rs));
            }
        }
    }

    return lista;
}

//buscar un pedido que pertenezca al cliente conectado:
public Pedido buscarPorCliente(String pedidoId, String clienteId)
        throws SQLException {

    String sql = "SELECT * FROM public.pedidos "
            + "WHERE id = ?::uuid AND cuenta_cliente_id = ?::uuid";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, pedidoId);
        ps.setString(2, clienteId);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return convertirPedido(rs);
            }
        }
    }

    return null;
}
//Recuperar los productos pedidos por un cliente en especifico
public List<PedidoDetalle> listarDetallesCliente(
        String pedidoId, String clienteId)
        throws SQLException {

    List<PedidoDetalle> lista = new ArrayList<>();

    String sql = """
        SELECT d.*
        FROM public.pedido_detalles d
        INNER JOIN public.pedidos p ON p.id = d.pedido_id
        WHERE d.pedido_id = ?::uuid
        AND p.cuenta_cliente_id = ?::uuid
        ORDER BY d.id
        """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, pedidoId);
        ps.setString(2, clienteId);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                PedidoDetalle d = new PedidoDetalle();

                d.setId(rs.getLong("id"));
                d.setPedidoId(rs.getString("pedido_id"));
                d.setProductoId(rs.getLong("producto_id"));
                d.setNombreProducto(rs.getString("nombre_producto"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecioUnitario(rs.getDouble("precio_unitario"));
                d.setSubtotal(rs.getDouble("subtotal"));

                lista.add(d);
            }
        }
    }

    return lista;
}
}
