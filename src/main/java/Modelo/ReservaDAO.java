package Modelo;

import java.sql.*;
import java.util.*;
import jakarta.servlet.http.HttpSession;

/** La base de datos es la fuente de verdad; cada sesión tiene su UUID privado. */
public class ReservaDAO {
    public static UUID id(HttpSession session) {
        synchronized (session) {
            UUID id = (UUID) session.getAttribute("reservaId");
            if (id == null) { id = UUID.randomUUID(); session.setAttribute("reservaId", id); }
            return id;
        }
    }
    public Map<Integer,Integer> cargar(UUID id) throws SQLException {
        Map<Integer,Integer> m = new LinkedHashMap<>();
        try (Connection c = Conexion.conectar(); PreparedStatement ps = c.prepareStatement(
                "SELECT producto_id,cantidad FROM public.reservas_stock WHERE carrito_id=? AND expira_en>clock_timestamp() ORDER BY producto_id")) {
            ps.setObject(1,id);
            try (ResultSet rs=ps.executeQuery()) { while(rs.next()) m.put(rs.getInt(1),rs.getInt(2)); }
        }
        return m;
    }
    public long vence(UUID id) throws SQLException {
        try (Connection c=Conexion.conectar(); PreparedStatement ps=c.prepareStatement(
                "SELECT COALESCE(EXTRACT(EPOCH FROM min(expira_en))*1000,0)::bigint FROM public.reservas_stock WHERE carrito_id=? AND expira_en>clock_timestamp()")) {
            ps.setObject(1,id); try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getLong(1);}
        }
    }
    /** Cantidad total deseada, no incremento. Bloqueo de fila serializa clientes distintos. */
    public boolean reservar(UUID id,int producto,int cantidad) throws SQLException {
        if(producto<=0 || cantidad<0) return false;
        try(Connection c=Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("SELECT id FROM public.productos WHERE id=? AND activo AND NOT eliminado FOR UPDATE")) {
                    p.setInt(1,producto); try(ResultSet r=p.executeQuery()){if(!r.next()){c.rollback();return false;}}
                }
                if(cantidad>0) {
                    try(PreparedStatement p=c.prepareStatement("SELECT stock_fisico-stock_reservado-stock_pedidos + COALESCE((SELECT cantidad FROM public.reservas_stock WHERE carrito_id=? AND producto_id=? AND expira_en>clock_timestamp()),0) FROM public.v_stock_disponible WHERE producto_id=?")) {
                        p.setObject(1,id);p.setInt(2,producto);p.setInt(3,producto);
                        try(ResultSet r=p.executeQuery()){if(!r.next() || cantidad>r.getLong(1)){c.rollback();return false;}}
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO public.reservas_stock(carrito_id,producto_id,cantidad,expira_en) VALUES (?,?,?,clock_timestamp()+interval '5 minutes') ON CONFLICT(carrito_id,producto_id) DO UPDATE SET cantidad=EXCLUDED.cantidad,expira_en=EXCLUDED.expira_en")) {
                        p.setObject(1,id);p.setInt(2,producto);p.setInt(3,cantidad);p.executeUpdate();
                    }
                    // Un único vencimiento para todo el carrito, renovado únicamente por una acción de compra.
                    try(PreparedStatement p=c.prepareStatement("UPDATE public.reservas_stock SET expira_en=clock_timestamp()+interval '5 minutes' WHERE carrito_id=? AND expira_en>clock_timestamp()")) {p.setObject(1,id);p.executeUpdate();}
                } else {
                    try(PreparedStatement p=c.prepareStatement("DELETE FROM public.reservas_stock WHERE carrito_id=? AND producto_id=?")){p.setObject(1,id);p.setInt(2,producto);p.executeUpdate();}
                }
                c.commit();return true;
            } catch(SQLException e){c.rollback();throw e;}
        }
    }
    public void liberar(UUID id) throws SQLException {
        try(Connection c=Conexion.conectar(); PreparedStatement p=c.prepareStatement("DELETE FROM public.reservas_stock WHERE carrito_id=?")){p.setObject(1,id);p.executeUpdate();}
    }
    public static void consumir(Connection c,UUID id,Map<Long,Integer> cantidades) throws SQLException {
        // Mismo orden de bloqueo entre compras para reducir deadlocks.
        for(long producto:new TreeSet<>(cantidades.keySet())) {
            try(PreparedStatement p=c.prepareStatement("SELECT id FROM public.productos WHERE id=? AND activo AND NOT eliminado FOR UPDATE")) {
                p.setLong(1,producto);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Producto no disponible");}
            }
        }
        for(var e:cantidades.entrySet()) {
            try(PreparedStatement p=c.prepareStatement("SELECT r.cantidad,v.stock_fisico-v.stock_reservado-v.stock_pedidos FROM public.reservas_stock r JOIN public.v_stock_disponible v ON v.producto_id=r.producto_id WHERE r.carrito_id=? AND r.producto_id=? AND r.expira_en>clock_timestamp() FOR UPDATE OF r")) {
                p.setObject(1,id);p.setLong(2,e.getKey());try(ResultSet r=p.executeQuery()){if(!r.next() || r.getInt(1)!=e.getValue() || r.getLong(2)<0)throw new SQLException("La reserva venció o el stock cambió. Actualiza el carrito.");}
            }
        }
        try(PreparedStatement p=c.prepareStatement("DELETE FROM public.reservas_stock WHERE carrito_id=?")){p.setObject(1,id);p.executeUpdate();}
    }
}
