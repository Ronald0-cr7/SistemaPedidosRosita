package servicio;

import Modelo.Pago;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MercadoPagoService {

    private static final String API_URL =
            "https://api.mercadopago.com/v1/orders";

    private static final String TOKEN =
            System.getenv("MP_ACCESS_TOKEN");

    private final HttpClient clienteHttp =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    // CREAR ORDEN DE PAGO
    public JsonObject crearOrden(
            Pago pago, String correo) throws Exception {

        validarToken();

        if (pago.getMonto() == null
                || pago.getMonto().signum() <= 0) {

            throw new IllegalArgumentException("Monto inválido");
        }

        if (pago.getId() == null
                || pago.getIdempotencyKey() == null) {

            throw new IllegalArgumentException(
                    "El pago debe tener identificadores");
        }

        if (!"PEN".equals(pago.getMoneda())) {
            throw new IllegalArgumentException("Moneda inválida");
        }

        BigDecimal monto = pago.getMonto()
                .setScale(2, RoundingMode.UNNECESSARY);

        // DATOS DE LA ORDEN
        JsonObject orden = new JsonObject();

        orden.addProperty("type", "online");
        orden.addProperty("processing_mode", "manual");
        orden.addProperty("total_amount", monto.toPlainString());
        orden.addProperty("external_reference", pago.getId());
        orden.addProperty("description", "Pedido Minimarket Rosita");

        // PRODUCTO
        JsonObject producto = new JsonObject();

        producto.addProperty("title", "Pedido Minimarket Rosita");
        producto.addProperty("quantity", 1);
        producto.addProperty("unit_price", monto.toPlainString());

        JsonArray items = new JsonArray();
        items.add(producto);

        orden.add("items", items);

        // DATOS DEL CLIENTE
        if (correo != null && !correo.isBlank()) {

            JsonObject comprador = new JsonObject();

            comprador.addProperty("email", correo);

            orden.add("payer", comprador);
        }
String baseUrl = System.getenv("MP_BASE_URL");

if (baseUrl != null && !baseUrl.isBlank()) {

    baseUrl = baseUrl.replaceAll("/+$", "");

    String retorno = baseUrl
            + "/ResultadoPago?pago="
            + pago.getId();

    JsonObject online = new JsonObject();

    online.addProperty("success_url", retorno);
    online.addProperty("failure_url", retorno);
    online.addProperty("pending_url", retorno);
    online.addProperty("auto_return", "all");

    JsonObject config = new JsonObject();

    config.add("online", online);

    orden.add("config", config);
}
        
        // ENVIAR SOLICITUD A MERCADO PAGO
        HttpRequest request = HttpRequest.newBuilder()

                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(20))

                .header("Authorization", "Bearer " + TOKEN)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")

                .header(
                    "X-Idempotency-Key",
                    pago.getIdempotencyKey().toString()
                )

                .POST(HttpRequest.BodyPublishers.ofString(
                        orden.toString()
                ))

                .build();

        HttpResponse<String> respuesta =
                clienteHttp.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (respuesta.statusCode() != 201) {

            throw new IllegalStateException(
                    "Error al crear orden. HTTP: "
                    + respuesta.statusCode()
            );
        }

        return JsonParser.parseString(
                respuesta.body()
        ).getAsJsonObject();
    }

    // CONSULTAR ORDEN DE PAGO
    public JsonObject consultarOrden(
            String mpOrderId) throws Exception {

        validarToken();

        if (mpOrderId == null
                || !mpOrderId.matches("[A-Za-z0-9_-]{1,100}")) {

            throw new IllegalArgumentException(
                    "Identificador de orden inválido");
        }

        HttpRequest request = HttpRequest.newBuilder()

                .uri(URI.create(API_URL + "/" + mpOrderId))
                .timeout(Duration.ofSeconds(15))

                .header("Authorization", "Bearer " + TOKEN)
                .header("Accept", "application/json")

                .GET()
                .build();

        HttpResponse<String> respuesta =
                clienteHttp.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (respuesta.statusCode() != 200) {

            throw new IllegalStateException(
                    "Error al consultar orden. HTTP: "
                    + respuesta.statusCode()
            );
        }

        return JsonParser.parseString(
                respuesta.body()
        ).getAsJsonObject();
    }

    // VALIDAR ACCESS TOKEN
    private void validarToken() {

        if (TOKEN == null || TOKEN.isBlank()) {

            throw new IllegalStateException(
                    "Falta configurar MP_ACCESS_TOKEN");
        }
    }
}