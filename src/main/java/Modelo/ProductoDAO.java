/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ronaldo
 */
public class ProductoDAO {
    
    //READ : Obtener la lista de productos

// READ: Obtener lista de productos con nombre de categoría
public List<Producto> listar() throws SQLException {

    List<Producto> lista = new ArrayList<>();

    String sql = "SELECT p.*, c.nombre AS nombre_categoria "
               + "FROM public.productos p "
               + "LEFT JOIN public.categorias c "
               + "ON p.categoria_id = c.id "
               + "WHERE p.eliminado = false "
               + "ORDER BY p.id";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            Producto p = new Producto();

            p.setId(rs.getInt("id"));
            p.setCategoriaId(rs.getInt("categoria_id"));
            p.setNombre(rs.getString("nombre"));
            p.setDescripcion(rs.getString("descripcion"));
            p.setPrecio(rs.getDouble("precio"));
            p.setStock(rs.getInt("stock"));
            p.setMinimo(rs.getInt("minimo"));
            p.setImagen(rs.getString("imagen"));
            p.setVencimiento(rs.getString("vencimiento"));
            p.setActivo(rs.getBoolean("activo"));
            p.setEliminado(rs.getBoolean("eliminado"));

            // NUEVO: Nombre real de la categoría
            p.setNombreCategoria(
                rs.getString("nombre_categoria")
            );

            lista.add(p);
        }

    } catch (SQLException e) {
        throw new SQLException(
            "Error al listar los productos", e
        );
    }

    return lista;
}

    public void agregar(Producto p) throws SQLException {

    String sql = "INSERT INTO public.productos "
            + "(categoria_id, nombre, descripcion, precio, stock, minimo, imagen, vencimiento, activo) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, p.getCategoriaId());
        ps.setString(2, p.getNombre());
        ps.setString(3, p.getDescripcion());
        ps.setDouble(4, p.getPrecio());
        ps.setInt(5, p.getStock());
        ps.setInt(6, p.getMinimo());
        ps.setString(7, p.getImagen());
       
        

        if (p.getVencimiento() == null || p.getVencimiento().isBlank()) {
            ps.setNull(8, java.sql.Types.DATE);
        } else {
            ps.setDate(8, java.sql.Date.valueOf(p.getVencimiento()));
        }
        //Activar automaticamente el producto 
        ps.setBoolean(9, true); //Sea visible en el catalogo
        ps.executeUpdate();
    }
}public Producto buscar(int id) throws SQLException {

    String sql = "SELECT * FROM productos WHERE id=? AND eliminado=false";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Producto p = new Producto();

            p.setId(rs.getInt("id"));
            p.setCategoriaId(rs.getInt("categoria_id"));
            p.setNombre(rs.getString("nombre"));
            p.setDescripcion(rs.getString("descripcion"));
            p.setPrecio(rs.getDouble("precio"));
            p.setStock(rs.getInt("stock"));
            p.setMinimo(rs.getInt("minimo"));
            p.setImagen(rs.getString("imagen"));
            p.setVencimiento(rs.getString("vencimiento"));
            p.setActivo(rs.getBoolean("activo"));

            return p;
        }
    }

    return null;
}



public boolean actualizar(Producto p) throws SQLException {

    String sql = "UPDATE public.productos SET "
               + "nombre=?, categoria_id=?, descripcion=?, "
               + "precio=?,stock=?, minimo=?, vencimiento=?, activo=?, imagen=? "
               + "WHERE id=? AND eliminado=false";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, p.getNombre());
        ps.setInt(2, p.getCategoriaId());
        ps.setString(3, p.getDescripcion());
        ps.setDouble(4, p.getPrecio());
        ps.setInt(5, p.getStock());
        ps.setInt(6, p.getMinimo());

        if (p.getVencimiento() == null || p.getVencimiento().isBlank()) {
            ps.setNull(7, java.sql.Types.DATE);
        } else {
            ps.setDate(7, java.sql.Date.valueOf(p.getVencimiento()));
        }

        ps.setBoolean(8, p.isActivo());
        ps.setString(9, p.getImagen());
        ps.setInt(10, p.getId());

        return ps.executeUpdate() > 0;
    }
}   

public boolean actualizarVisibilidad(int id, boolean activo)
        throws SQLException {

    String sql = """
        UPDATE public.productos
        SET activo = ?
        WHERE id = ?
          AND eliminado = FALSE
        """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setBoolean(1, activo);
        ps.setInt(2, id);

        return ps.executeUpdate() > 0;
    }
}
             
      //Eliminar productos 

// DELETE: Eliminar producto
public boolean eliminar(int id) throws SQLException {

    String sql = "UPDATE public.productos "
               + "SET eliminado = true, activo = false "
               + "WHERE id = ?";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);

        return ps.executeUpdate() > 0;
    }
}
//Agregar un nuevo metodo para mostrar el catalogo de productos
public List<Producto> listarCatalogo() throws SQLException {

    List<Producto> lista = new ArrayList<>(); //recuperar los datos en la BD
    

    String sql = """
        SELECT
            p.id,
            p.categoria_id,
            p.nombre,
            p.descripcion,
            p.precio,
            p.stock,
            p.minimo,
            p.imagen,
            p.vencimiento,
            p.activo,
            p.eliminado,
            c.nombre AS nombre_categoria
        FROM public.productos p
        LEFT JOIN public.categorias c
            ON p.categoria_id = c.id
        WHERE p.activo = TRUE
          AND p.eliminado = FALSE
          AND p.stock > 0
        ORDER BY p.nombre ASC
        """;

    try (Connection cn = Conexion.conectar(); //Nos conectamos a la base de datos 
         PreparedStatement ps = cn.prepareStatement(sql); //Preparar la consulta sql
            
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            Producto p = new Producto(); //Recuperar los registros a traves del objeto de mi clase producto 
//Asginar los parametros
            p.setId(rs.getInt("id"));
            p.setCategoriaId(rs.getInt("categoria_id"));
            p.setNombre(rs.getString("nombre"));
            p.setDescripcion(rs.getString("descripcion"));
            p.setPrecio(rs.getDouble("precio"));
            p.setStock(rs.getInt("stock"));
            p.setMinimo(rs.getInt("minimo"));
            p.setImagen(rs.getString("imagen"));
            p.setVencimiento(rs.getString("vencimiento"));
            p.setActivo(rs.getBoolean("activo"));
            p.setEliminado(rs.getBoolean("eliminado"));
            p.setNombreCategoria(
                rs.getString("nombre_categoria")
            );

            lista.add(p);
        }
    }

    return lista;
}

    // READ: Listar productos por categoría
    public List<Producto> listarProductosPorCategoria(int categoriaId)
            throws SQLException {

        List<Producto> lista = new ArrayList<>();

        String sql = """
            SELECT p.*, c.nombre AS nombre_categoria
            FROM public.productos p
            INNER JOIN public.categorias c
                ON p.categoria_id = c.id
            WHERE p.categoria_id = ?
              AND p.activo = TRUE
              AND p.eliminado = FALSE
              AND p.stock > 0
              AND c.activo = TRUE
            ORDER BY p.nombre ASC
            """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, categoriaId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Producto p = new Producto();

                    p.setId(rs.getInt("id"));
                    p.setCategoriaId(rs.getInt("categoria_id"));
                    p.setNombre(rs.getString("nombre"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setPrecio(rs.getDouble("precio"));
                    p.setStock(rs.getInt("stock"));
                    p.setMinimo(rs.getInt("minimo"));
                    p.setImagen(rs.getString("imagen"));
                    p.setVencimiento(rs.getString("vencimiento"));
                    p.setActivo(rs.getBoolean("activo"));
                    p.setEliminado(rs.getBoolean("eliminado"));
                    p.setNombreCategoria(
                        rs.getString("nombre_categoria")
                    );

                    lista.add(p);
                }
            }
        }

        return lista;
    }
    
public java.util.Map<Integer, String> listarCategorias()
        throws SQLException {

    java.util.Map<Integer, String> categorias =
            new java.util.LinkedHashMap<>();

    String sql = """
        SELECT id, nombre
        FROM public.categorias
        WHERE activo = TRUE
        ORDER BY nombre ASC
        """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            categorias.put(
                rs.getInt("id"),
                rs.getString("nombre")
            );
        }
    }

    return categorias;
}



        
                
    }

