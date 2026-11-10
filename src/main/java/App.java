import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

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
        FactDatabase database = new FactDatabase(DB_URL);
        FactsRouter router = new FactsRouter(database);
        FactsHandler handler = new FactsHandler(router);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/facts", handler);
        server.start();
        System.out.println("Cat Facts API running on http://localhost:" + PORT + "/facts");
    }
}
