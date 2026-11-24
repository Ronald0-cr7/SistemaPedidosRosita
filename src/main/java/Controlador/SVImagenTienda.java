package Controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/imagenTienda")
public class SVImagenTienda extends HttpServlet {

    private final Path carpetaImagenes = Paths.get(
        System.getProperty("user.home"),
        "MinimarketRosita",
        "tienda"
    );

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

        Path imagen = carpetaImagenes.resolve(nombre);

        if (!Files.isRegularFile(imagen)) {
            response.sendError(404);
            return;
        }

        if (nombre.endsWith(".jpg")) {
            response.setContentType("image/jpeg");
        } else if (nombre.endsWith(".png")) {
            response.setContentType("image/png");
        } else {
            response.setContentType("image/webp");
        }

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLengthLong(Files.size(imagen));

        Files.copy(imagen, response.getOutputStream());
    }
}