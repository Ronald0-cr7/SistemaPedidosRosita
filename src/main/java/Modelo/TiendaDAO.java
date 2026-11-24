/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author ronaldo
 */
public class TiendaDAO {
    //Mostrar la informacion de la tienda que esta en la BD
    public Tienda mostrarInfo() throws SQLException {
        
        String sql = "SELECT * FROM public.Tienda WHERE id=1";
        //int id, String nombre, String direccion, String maps, String whatsapp, String imagen
        try(Connection con = Conexion.conectar(); //Conectamos a la BD 
                PreparedStatement ps = con.prepareStatement(sql); 
                ResultSet rs = ps.executeQuery()){
            if (rs.next()){
                Tienda t = new Tienda();
                //asignar los parametros para ejecutar la consulta sql 
                t.setId(rs.getInt("id"));
                t.setNombre(rs.getString("nombre"));
                t.setDireccion(rs.getString("direccion"));
                t.setMaps(rs.getString("maps"));
                t.setWhatsapp(rs.getString("whatsapp"));
                t.setImagen(rs.getString("imagen"));
                
                return t;
                
            }
            
            
            
        } catch (SQLException e) {
        throw new SQLException(
            "Error al mostrar la informacion de la tienda", e
        );
     
        
        
                
                
                
        
    }
        return null;
        
    
} 
    //UPDATE: Actualizar los datos de la informacion de la tienda 
    public boolean actualizar(Tienda t) throws SQLException {
        
        String sql = "UPDATE public.Tienda SET nombre=?, direccion=?, maps=?, whatsapp=?, imagen=? WHERE id=1";
        try(Connection con = Conexion.conectar(); //Conectamos a la BD 
                PreparedStatement ps = con.prepareStatement(sql); 
                ){
            //preparamos los parametros para ejecutar la consulta UPDATE
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getDireccion());
            ps.setString(3, t.getMaps());
            ps.setString(4, t.getWhatsapp());
            ps.setString(5, t.getImagen());
            
            return ps.executeUpdate()>0;
            
        }
        
    }
}
