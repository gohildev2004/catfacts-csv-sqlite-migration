import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Cat Facts API, backed by SQLite.
 *
 *   GET  /facts        every fact
 *   GET  /facts/{id}   one fact, or 404
 *   POST /facts        add a fact; body is {"fact": "..."}
 */
public class App {

    static final int PORT = 8080;
    static final String DB_URL = "jdbc:sqlite:catfacts.db";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/facts", new FactsHandler(new FactDatabase(DB_URL)));
        server.start();
        System.out.println("Cat Facts API running on http://localhost:" + PORT + "/facts");
    }

    static class FactsHandler implements HttpHandler {

        private final FactDatabase database;

        FactsHandler(FactDatabase database) {
            this.database = database;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                route(exchange);
            } catch (Exception e) {
                // Print the real problem in the server terminal, send a clean 500 to the client.
                e.printStackTrace();
                sendJson(exchange, 500, new ErrorBody("internal server error"));
            }
        }

        /** Decides which endpoint a request is for. */
        void route(HttpExchange exchange) throws IOException, SQLException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            // "/facts" or "/facts/"  ->  returns the whole collection
            if (path.equals("/facts") || path.equals("/facts/")) {
                switch (method) {
                    case "GET"  -> sendJson(exchange, 200, database.getAllFacts());
                    case "POST" -> createFact(exchange);
                    default     -> sendJson(exchange, 405, new ErrorBody("method not allowed"));
                }
                return;
            }

            // Anything else that merely starts with "/facts", like "/factsabc"
            if (!path.startsWith("/facts/")) {
                sendJson(exchange, 404, new ErrorBody("not found"));
                return;
            }

            // "/facts/{id}"  ->  returns one fact with the provided id
            String idText = path.substring("/facts/".length());
            int id;
            try {
                id = Integer.parseInt(idText);
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, new ErrorBody("id must be a whole number"));
                return;
            }
            if (method.equals("GET")) {
                Optional<CatFact> found = database.findFactById(id);
                if (found.isPresent()) {
                    sendJson(exchange, 200, found.get());
                } else {
                    sendJson(exchange, 404, new ErrorBody("no fact with id " + id));
                }
            } else {
                sendJson(exchange, 405, new ErrorBody("method not allowed"));
            }
        }

        /** POST /facts */
        void createFact(HttpExchange exchange) throws IOException, SQLException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            NewFact input;
            try {
                input = Json.gson.fromJson(body, NewFact.class);
            } catch (JsonParseException e) {
                sendJson(exchange, 400, new ErrorBody("body must be JSON like {\"fact\": \"...\"}"));
                return;
            }
            if (input == null || input.fact() == null || input.fact().isBlank()) {
                sendJson(exchange, 400, new ErrorBody("\"fact\" is required and cannot be blank"));
                return;
            }
            String text = input.fact().trim();
            CatFact created = database.insertFact(text);

            exchange.getResponseHeaders().set("Location", "/facts/" + created.id());
            sendJson(exchange, 201, created);
        }

        // ---- Helper ------------------------------------------------------

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
}
