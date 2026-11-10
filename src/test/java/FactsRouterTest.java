import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class FactsRouterTest {

    @TempDir
    Path tempDir;

    // Before the split this was impossible: createFact needed a real HttpExchange to read the body
    // and set the Location header, and it always wrote to catfacts.db. Now the router takes plain
    // strings, returns a Response, and gets a test database through FactDatabase's constructor.
    @Test
    void postingAFactReturns201WithLocationOfTheNewFact() throws Exception {
        String url = "jdbc:sqlite:" + tempDir.resolve("test.db");
        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE facts (id INTEGER PRIMARY KEY AUTOINCREMENT, fact TEXT NOT NULL)");
        }
        FactsRouter router = new FactsRouter(new FactDatabase(url));

        Response response = router.route("POST", "/facts", "{\"fact\": \"  Cats have three eyelids  \"}");

        assertEquals(201, response.status());
        assertEquals(new CatFact(1, "Cats have three eyelids"), response.body());
        assertEquals("/facts/1", response.location());
    }
}
