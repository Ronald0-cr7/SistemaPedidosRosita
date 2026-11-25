/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ronaldo
 */
//Contener los metodos necesarios para filtrar los productos que piden los clientes

public class CarritoDAO {
    //Definir un metodo para buscar el producto por su id
      public Carrito buscarProducto(int productoId)
            throws SQLException {

        String sql = """
            SELECT
                p.id,
                p.nombre,
                p.imagen,
                p.precio,
                v.stock_disponible AS stock,
                c.nombre AS categoria
            FROM public.productos p
            JOIN public.v_stock_disponible v ON v.producto_id=p.id
            INNER JOIN public.categorias c
                ON p.categoria_id = c.id
            WHERE p.id = ?
              AND p.activo = TRUE
              AND p.eliminado = FALSE
              AND c.activo = TRUE
            """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productoId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Carrito item = new Carrito();
//Recuperar los datos de los productos que agregamos al carrito
                    item.setProductoId(
                        rs.getInt("id")
                    );

                    item.setNombre(
                        rs.getString("nombre")
                    );

                    item.setImagen(
                        rs.getString("imagen")
                    );

                    item.setCategoria(
                        rs.getString("categoria")
                    );

                    item.setPrecio(
                        rs.getDouble("precio")
                    );

                    item.setStockDisponible(
                        rs.getInt("stock")
                    );

                    item.setCantidad(1);

                    return item;
                }
            }
        }

        return null;
    }

    // Recuperar los productos seleccionados
    // Recibe ID de producto y cantidad
    public List<Carrito> listarProductos(
            Map<Integer, Integer> seleccionados)
            throws SQLException {

        List<Carrito> lista = new ArrayList<>();

        if (seleccionados == null
                || seleccionados.isEmpty()) {

            return lista;
        }

        for (Map.Entry<Integer, Integer> entrada
                : seleccionados.entrySet()) {

            int id = entrada.getKey();
            int cantidad = entrada.getValue();

            if (id <= 0 || cantidad <= 0) {
                throw new IllegalArgumentException(
                    "Producto o cantidad inválida."
                );
            }

            Carrito item = buscarProducto(id);

            if (item == null) {
                throw new IllegalArgumentException(
                    "Producto no disponible: " + id
                );
            }

            item.setStockDisponible(item.getStockDisponible() + cantidad);

            item.setCantidad(cantidad);

            lista.add(item);
        }

        return lista;
    }

    // Verificar stock actual
    public boolean verificarStock(
            int productoId,
            int cantidad) throws SQLException {

        if (cantidad <= 0) {
            return false;
        }

        Carrito producto = buscarProducto(productoId);

        return producto != null
                && cantidad <= producto.getStockDisponible();
    }

    
    // Calcular subtotal general del carrito
    public double calcularSubtotal(List<Carrito> productos) {

        double subtotal = 0.0;

        if (productos == null || productos.isEmpty()) {
            return subtotal;
        }

        for (Carrito item : productos) {

            subtotal += item.getSubtotal();

        }

        return Math.round(subtotal * 100.0) / 100.0;
    }

    // Calcular costo de delivery
    public double calcularDelivery(String tipoEntrega) {

        if ("DELIVERY".equalsIgnoreCase(tipoEntrega)) {
            return 5.00;
        }

        return 0.00;
    }

    // Calcular total del pedido
    public double calcularTotal(
            List<Carrito> productos,
            String tipoEntrega) {

        double subtotal = calcularSubtotal(productos);

        double delivery = calcularDelivery(tipoEntrega);

        double total = subtotal + delivery;

        return Math.round(total * 100.0) / 100.0;
    }
}
