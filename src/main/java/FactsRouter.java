import com.google.gson.JsonParseException;

import java.sql.SQLException;
import java.util.Optional;

class FactsRouter {

    private final FactDatabase database;

    FactsRouter(FactDatabase database) {
        this.database = database;
    }

    /** Decides which endpoint a request is for. */
    Response route(String method, String path, String body) throws SQLException {
        // "/facts" or "/facts/"  ->  returns the whole collection
        if (path.equals("/facts") || path.equals("/facts/")) {
            return switch (method) {
                case "GET"  -> new Response(200, database.getAllFacts(), null);
                case "POST" -> createFact(body);
                default     -> new Response(405, new ErrorBody("method not allowed"), null);
            };
        }

        // Anything else that merely starts with "/facts", like "/factsabc"
        if (!path.startsWith("/facts/")) {
            return new Response(404, new ErrorBody("not found"), null);
        }

        // "/facts/{id}"  ->  returns one fact with the provided id
        String idText = path.substring("/facts/".length());
        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            return new Response(400, new ErrorBody("id must be a whole number"), null);
        }
        if (method.equals("GET")) {
            Optional<CatFact> found = database.findFactById(id);
            if (found.isPresent()) {
                return new Response(200, found.get(), null);
            }
            return new Response(404, new ErrorBody("no fact with id " + id), null);
        }
        return new Response(405, new ErrorBody("method not allowed"), null);
    }

    /** POST /facts */
    Response createFact(String body) throws SQLException {
        NewFact input;
        try {
            input = Json.gson.fromJson(body, NewFact.class);
        } catch (JsonParseException e) {
            return new Response(400, new ErrorBody("body must be JSON like {\"fact\": \"...\"}"), null);
        }
        if (input == null || input.fact() == null || input.fact().isBlank()) {
            return new Response(400, new ErrorBody("\"fact\" is required and cannot be blank"), null);
        }
        String text = input.fact().trim();
        if (text.contains("\n") || text.contains("\r")) {
            return new Response(400, new ErrorBody("\"fact\" must be a single line"), null);
        }
        CatFact created = database.insertFact(text);
        return new Response(201, created, "/facts/" + created.id());
    }
}
