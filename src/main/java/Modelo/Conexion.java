
package Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
        "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require&connectTimeout=10";

    private static final String USUARIO =
        "postgres.lyosejrliykxyydqjcmf";

    private static final String CLAVE =
        System.getenv("DB_PASSWORD");

    public static Connection conectar() throws SQLException {

        if (CLAVE == null || CLAVE.isBlank()) {
            throw new SQLException("Falta configurar DB_PASSWORD");
        }

        // Cargar el driver de PostgreSQL
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "No se encontró el driver PostgreSQL", e
            );
        }

        // Conectar con Supabase
        return DriverManager.getConnection(
            URL, USUARIO, CLAVE
        );
    }
}
