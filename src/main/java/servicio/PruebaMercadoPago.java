package servicio;

import Modelo.Pago;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.UUID;

public class PruebaMercadoPago {

    public static void main(String[] args) {

        try {

            // CREAR PAGO DE PRUEBA
            Pago pago = new Pago();

            pago.setId(UUID.randomUUID().toString());
            pago.setMonto(new BigDecimal("10.00"));
            pago.setMoneda("PEN");
            pago.setEstado("Pendiente");
            pago.setIdempotencyKey(UUID.randomUUID());

            MercadoPagoService servicio =
                    new MercadoPagoService();

            // CREAR ORDEN
            JsonObject respuesta =
                    servicio.crearOrden(pago, null);

            // RESULTADOS
            System.out.println(
                    "ORDEN CREADA CORRECTAMENTE");

            System.out.println(
                    "ID: " + respuesta.get("id"));

            System.out.println(
                    "Estado: " + respuesta.get("status"));

            System.out.println(
                    "Monto: " + respuesta.get("total_amount"));

            System.out.println(
                    "Checkout: "
                    + respuesta.get("checkout_url"));

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}