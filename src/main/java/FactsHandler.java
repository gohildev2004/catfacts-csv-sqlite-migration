import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Reads the request off the HttpExchange, asks the router what to do, and writes its Response back.
 * This is the only class that touches HttpExchange.
 */
class FactsHandler implements HttpHandler {

    private final FactsRouter router;

    FactsHandler(FactsRouter router) {
        this.router = router;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            Response response = router.route(method, path, body);

            if (response.location() != null) {
                exchange.getResponseHeaders().set("Location", response.location());
            }
            sendJson(exchange, response.status(), response.body());
        } catch (Exception e) {
            // Print the real problem in the server terminal, send a clean 500 to the client.
            e.printStackTrace();
            sendJson(exchange, 500, new ErrorBody("internal server error"));
        }
    }

    /** Converts any object to JSON with Gson and sends it with the given status. */
    void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = Json.gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
