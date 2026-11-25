package Controlador;
import Modelo.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
@WebServlet("/StockDisponible")
public class SVStockDisponible extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");resp.setHeader("Cache-Control","no-store");
        try {
            var session=req.getSession(); long cantidad=0; long vence=0;
            synchronized(session){var dao=new ReservaDAO();var id=ReservaDAO.id(session);var items=dao.cargar(id);session.setAttribute("carrito",items);for(int n:items.values())cantidad+=n;vence=dao.vence(id);}
            var productos=new ProductoDAO().listarCatalogo();
            StringBuilder json=new StringBuilder("{\"cantidad\":").append(cantidad).append(",\"vence\":").append(vence).append(",\"stock\":{");boolean primero=true;
            for(var p:productos){if(!primero)json.append(',');primero=false;json.append('"').append(p.getId()).append("\":").append(p.getStock());}
            resp.getWriter().print(json.append("}}").toString());
        } catch(SQLException e){resp.sendError(503);}
    }
}
