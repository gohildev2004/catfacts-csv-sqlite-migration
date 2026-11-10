import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FactDatabaseTest {

    @TempDir
    Path tempDir;

    @Test
    void getAllFactsReturnsEveryRowInIdOrder() throws Exception {
        Path db = tempDir.resolve("test.db");
        String url = "jdbc:sqlite:" + db;
        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE facts (id INTEGER PRIMARY KEY AUTOINCREMENT, fact TEXT NOT NULL)");
            s.executeUpdate("INSERT INTO facts (id, fact) VALUES (1, 'Cats purr')");
            s.executeUpdate("INSERT INTO facts (id, fact) VALUES (2, 'Cats, oddly, cannot taste sweetness')");
        }

        List<CatFact> facts = new FactDatabase(url).getAllFacts();

        assertEquals(2, facts.size());
        assertEquals(1, facts.get(0).id());
        assertEquals("Cats, oddly, cannot taste sweetness", facts.get(1).fact());
    }

    @Test
    void findFactByIdReturnsFactWhenIdExists() throws Exception {
        Path db = tempDir.resolve("test.db");
        String url = "jdbc:sqlite:" + db;
        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE facts (id INTEGER PRIMARY KEY AUTOINCREMENT, fact TEXT NOT NULL)");
            s.executeUpdate("INSERT INTO facts (id, fact) VALUES (1, 'Cats purr')");
            s.executeUpdate("INSERT INTO facts (id, fact) VALUES (2, 'Cats, oddly, cannot taste sweetness')");
        }

        Optional<CatFact> found = new FactDatabase(url).findFactById(2);

        assertTrue(found.isPresent());
        assertEquals(new CatFact(2, "Cats, oddly, cannot taste sweetness"), found.get());
    }

    @Test
    void findFactByIdReturnsEmptyWhenIdMissing() throws Exception {
        Path db = tempDir.resolve("test.db");
        String url = "jdbc:sqlite:" + db;
        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE facts (id INTEGER PRIMARY KEY AUTOINCREMENT, fact TEXT NOT NULL)");
            s.executeUpdate("INSERT INTO facts (id, fact) VALUES (1, 'Cats purr')");
            s.executeUpdate("INSERT INTO facts (id, fact) VALUES (2, 'Cats, oddly, cannot taste sweetness')");
        }

        Optional<CatFact> found = new FactDatabase(url).findFactById(99);

        assertFalse(found.isPresent());
    }
}
