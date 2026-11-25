package Controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/imagenProducto")
public class SVImagenProductos extends HttpServlet {

    private static final String STORAGE_URL =
        "https://lyosejrliykxyydqjcmf.supabase.co"
        + "/storage/v1/object/public/productos/";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        String nombre = request.getParameter("archivo");

        if (nombre == null || !nombre.matches(
                "[a-fA-F0-9-]{36}\\.(jpg|png|webp)")) {
            response.sendError(400);
            return;
        }

        response.sendRedirect(STORAGE_URL + nombre);
    }
}
