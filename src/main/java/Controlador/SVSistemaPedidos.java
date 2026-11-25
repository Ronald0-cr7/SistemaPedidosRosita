/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Controlador;

import Modelo.Producto;
import Modelo.ProductoDAO;
import Modelo.Tienda;
import Modelo.TiendaDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet(name = "SVSistemaPedidos", urlPatterns = {"/SVSistemaPedidos",
"/IndexSistema",
"/ListaProductos",
"/ProductosPorCategoria"})
public class SVSistemaPedidos extends HttpServlet {


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        response.setContentType("text/html;charset=UTF-8");
        
                String ruta = request.getServletPath(); //contener los enlaces webServlets
                //Recuperar los datos de la tienda que esta en la BD
                TiendaDAO daotienda = new TiendaDAO();
                //Recuperar los datos de los productos
                ProductoDAO daoproducto = new ProductoDAO();
                var session = request.getSession();
                synchronized (session) {
                    session.setAttribute("carrito", new Modelo.ReservaDAO().cargar(Modelo.ReservaDAO.id(session)));
                }
                response.setHeader("Cache-Control", "no-store");
                
                
                try {
                    //Index de la pagina web y dentro de esta pagina va a mostrar
                    //el catalogo de productos y la info de la tienda
                    
    if (ruta.equals("/IndexSistema")) {
        Tienda tienda = daotienda.mostrarInfo();
        List<Producto> productos = daoproducto.listarCatalogo();
        request.setAttribute("infoTienda", tienda);
        request.setAttribute("productosCatalogo", productos);
        request.getRequestDispatcher(
            "/WEB-INF/vistas/tienda/indexPedidos.jsp"
        ).forward(request, response);

        return;
    } 

else if (ruta.equals("/ListaProductos")) {

    List<Producto> productos = daoproducto.listarCatalogo();

    request.setAttribute("productosCatalogo", productos);

    request.getRequestDispatcher(
        "/WEB-INF/vistas/tienda/CatalogoProductos.jsp"
    ).forward(request, response);

    return;
}

else if (ruta.equals("/ProductosPorCategoria")) {

    // 1. Recuperar el ID de la categoría desde la URL
    String categoria = request.getParameter("categoria");

    // 2. Verificar que se haya enviado una categoría
    if (categoria == null || categoria.isBlank()) {
        response.sendError(
            HttpServletResponse.SC_BAD_REQUEST,
            "Debe seleccionar una categoría"
        );
        return;
    }

    try {
        // 3. Convertir el ID de String a int
        int categoriaId = Integer.parseInt(categoria);

        if (categoriaId <= 0) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "El ID de categoría no es válido"
            );
            return;
        }

        // 4. Consultar los productos de esa categoría
        List<Producto> productos =
            daoproducto.listarProductosPorCategoria(categoriaId);

        // 5. Enviar los productos al JSP
        request.setAttribute("productosCatalogo", productos);

        // 6. Enviar el ID de la categoría seleccionada
        request.setAttribute("categoriaSeleccionada", categoriaId);

        // 7. Mostrar el catálogo filtrado
        request.getRequestDispatcher(
            "/WEB-INF/vistas/tienda/CatalogoProductos.jsp"
        ).forward(request, response);

        return;

    } catch (NumberFormatException e) {

        response.sendError(
            HttpServletResponse.SC_BAD_REQUEST,
            "El parámetro categoria debe ser numérico"
        );
        return;
    }
}



} catch (ServletException | IOException e) {
    throw new ServletException("Error al cargar el inicio del sistema", e);
}
        
       
    }

   
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Error consultando el catálogo", ex);
        }
    }

   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Error consultando el catálogo", ex);
        }
    }

 
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
