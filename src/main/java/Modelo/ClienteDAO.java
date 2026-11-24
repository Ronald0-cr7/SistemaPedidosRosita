package Modelo;

import Modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class ClienteDAO {

    public boolean registrar(Cliente cliente, String password)
            throws SQLException {

        String sql = """
            INSERT INTO public.clientes
            (nombres, apellidos, correo, telefono, dni, password_hash)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String hash = BCrypt.hashpw(
                password, BCrypt.gensalt(12)
        );

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombres());
            ps.setString(2, cliente.getApellidos());
            ps.setString(3, cliente.getCorreo());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getDni());
            ps.setString(6, hash);

            return ps.executeUpdate() == 1;
        }
    }
    public Cliente iniciarSesion(String correo, String password)
        throws SQLException {

    String sql = """
        SELECT id, nombres, apellidos, correo,
               telefono, dni, password_hash, activo
        FROM public.clientes
        WHERE correo = ? AND activo = true
        """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, correo);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                String hash = rs.getString("password_hash");

                if (BCrypt.checkpw(password, hash)) {

                    Cliente cliente = new Cliente();

                    cliente.setId(rs.getString("id"));
                    cliente.setNombres(rs.getString("nombres"));
                    cliente.setApellidos(rs.getString("apellidos"));
                    cliente.setCorreo(rs.getString("correo"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setDni(rs.getString("dni"));
                    cliente.setActivo(rs.getBoolean("activo"));

                    return cliente;
                }
            }
        }
    }

    return null;
}
    

    
}