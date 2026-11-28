package Modelo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PagoDAO {

    // REGISTRAR PAGO PENDIENTE
    public Pago registrarPendiente(
            Connection con, String pedidoId) throws SQLException {

        String sqlPedido = """
            SELECT subtotal + costo_delivery AS monto
            FROM public.pedidos
            WHERE id = ?::uuid
            AND metodo_pago = 'MERCADO_PAGO'
            AND estado = 'Pendiente'
            FOR UPDATE
            """;

        BigDecimal monto;

        try (PreparedStatement ps = con.prepareStatement(sqlPedido)) {

            ps.setString(1, pedidoId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                        "El pedido no está disponible para pagar");
                }

                monto = rs.getBigDecimal("monto");
            }
        }

        // Verificar si ya existe un pago activo
        String sqlActivo = """
            SELECT *
            FROM public.pagos
            WHERE pedido_id = ?::uuid
            AND estado IN ('Pendiente', 'Aprobado')
            """;

        try (PreparedStatement ps = con.prepareStatement(sqlActivo)) {

            ps.setString(1, pedidoId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Pago pago = convertirPago(rs);

                    if ("Aprobado".equals(pago.getEstado())) {
                        throw new SQLException(
                            "El pedido ya fue pagado");
                    }

                    return pago;
                }
            }
        }

        // Crear un nuevo intento de pago
        String sql = """
            INSERT INTO public.pagos (
                pedido_id,
                monto,
                moneda,
                estado
            )
            VALUES (?::uuid, ?, 'PEN', 'Pendiente')
            RETURNING *
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pedidoId);
            ps.setBigDecimal(2, monto);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirPago(rs);
                }
            }
        }

        throw new SQLException("No se pudo registrar el pago");
    }

    // BUSCAR PAGO POR ID
    public Pago buscarPorId(String id) throws SQLException {

        String sql = """
            SELECT *
            FROM public.pagos
            WHERE id = ?::uuid
            """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirPago(rs);
                }
            }
        }

        return null;
    }

    // BUSCAR ÚLTIMO PAGO POR PEDIDO
    public Pago buscarPorPedido(String pedidoId) throws SQLException {

        String sql = """
            SELECT *
            FROM public.pagos
            WHERE pedido_id = ?::uuid
            ORDER BY
                CASE WHEN estado = 'Aprobado' THEN 0 ELSE 1 END,
                creado_en DESC
            LIMIT 1
            """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pedidoId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirPago(rs);
                }
            }
        }

        return null;
    }

    // BUSCAR PAGO POR ID DE MERCADO PAGO
    public Pago buscarPorMpOrderId(String mpOrderId)
            throws SQLException {

        String sql = """
            SELECT *
            FROM public.pagos
            WHERE mp_order_id = ?
            """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mpOrderId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirPago(rs);
                }
            }
        }

        return null;
    }

    // GUARDAR ORDEN GENERADA POR MERCADO PAGO
    public boolean actualizarOrdenMP(
            String pagoId,
            String mpOrderId,
            String checkoutUrl,
            String mpStatus,
            String mpStatusDetail,
            OffsetDateTime expiraEn) throws SQLException {

        String sql = """
            UPDATE public.pagos
            SET mp_order_id = ?,
                checkout_url = ?,
                mp_status = ?,
                mp_status_detail = ?,
                expira_en = ?,
                actualizado_en = NOW()
            WHERE id = ?::uuid
            AND estado = 'Pendiente'
            AND (mp_order_id IS NULL OR mp_order_id = ?)
            """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mpOrderId);
            ps.setString(2, checkoutUrl);
            ps.setString(3, mpStatus);
            ps.setString(4, mpStatusDetail);
            ps.setObject(5, expiraEn);
            ps.setString(6, pagoId);
            ps.setString(7, mpOrderId);

            return ps.executeUpdate() > 0;
        }
    }

    // VERIFICAR SI EXISTE UN PAGO APROBADO
    public boolean existePagoAprobado(String pedidoId)
            throws SQLException {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM public.pagos
                WHERE pedido_id = ?::uuid
                AND estado = 'Aprobado'
            )
            """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pedidoId);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    // CONVERTIR RESULTSET EN OBJETO PAGO
    private Pago convertirPago(ResultSet rs)
            throws SQLException {

        Pago pago = new Pago();

        pago.setId(rs.getString("id"));
        pago.setPedidoId(rs.getString("pedido_id"));
        pago.setMpOrderId(rs.getString("mp_order_id"));
        pago.setMonto(rs.getBigDecimal("monto"));
        pago.setMoneda(rs.getString("moneda"));
        pago.setEstado(rs.getString("estado"));
        pago.setMpStatus(rs.getString("mp_status"));
        pago.setMpStatusDetail(rs.getString("mp_status_detail"));
        pago.setCheckoutUrl(rs.getString("checkout_url"));

        pago.setIdempotencyKey(
            rs.getObject("idempotency_key", UUID.class)
        );

        pago.setCreadoEn(
            rs.getObject("creado_en", OffsetDateTime.class)
        );

        pago.setExpiraEn(
            rs.getObject("expira_en", OffsetDateTime.class)
        );

        pago.setFechaPago(
            rs.getObject("fecha_pago", OffsetDateTime.class)
        );

        pago.setActualizadoEn(
            rs.getObject("actualizado_en", OffsetDateTime.class)
        );

        return pago;
    }
    public boolean actualizarEstadoVerificado(
        String pagoId,
        String mpOrderId,
        String estado,
        String mpStatus,
        String mpStatusDetail,
        OffsetDateTime mpActualizadoEn)
        throws SQLException {

    String sql = """
        UPDATE public.pagos
        SET estado = ?,
            mp_status = ?,
            mp_status_detail = ?,
            mp_actualizado_en = ?,
            fecha_pago = CASE
                WHEN ? = 'Aprobado'
                THEN COALESCE(fecha_pago, NOW())
                ELSE fecha_pago
            END,
            actualizado_en = NOW()
        WHERE id = ?::uuid
        AND mp_order_id = ?
        AND (
            mp_actualizado_en IS NULL
            OR mp_actualizado_en < ?
        )
        """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, estado);
        ps.setString(2, mpStatus);
        ps.setString(3, mpStatusDetail);
        ps.setObject(4, mpActualizadoEn);
        ps.setString(5, estado);
        ps.setString(6, pagoId);
        ps.setString(7, mpOrderId);
        ps.setObject(8, mpActualizadoEn);

        return ps.executeUpdate() > 0;
    }
}
}