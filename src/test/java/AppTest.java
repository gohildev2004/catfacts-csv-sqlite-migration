/*
 * Note: everything passes except loadFromCsvTwiceDoesNotDoubleFacts (question 17).
 * loadFromCsv never clears App.facts before reading, so calling it twice gives 20 facts instead of 10.
 * Since facts is static it also carries over between tests, so I reset it in @BeforeEach
 * to keep the other loadFromCsv tests from depending on which order they run in.
 */
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void resetFacts() {
        App.facts.clear();
        App.nextId = 1;
    }

    @Test
    void serializedCatFactContainsBothFields() {
        CatFact catFact = new CatFact(7, "Cats have five toes in front");

        String json = App.gson.toJson(catFact);

        assertTrue(json.contains("\"id\": 7"));
        assertTrue(json.contains("Cats have five toes in front"));
    }

    @Test
    void jsonBodyDeserializesIntoNewFact() {
        String json = "{\"fact\": \"Cats can jump really high\"}";

        NewFact newFact = App.gson.fromJson(json, NewFact.class);

        assertEquals("Cats can jump really high", newFact.fact());
    }

    @Test
    void apostropheStaysAnApostrophe() {
        CatFact catFact = new CatFact(2, "A cat's nose is unique");

        String json = App.gson.toJson(catFact);

        assertTrue(json.contains("cat's"));
        assertFalse(json.contains("\\u0027"));
    }

    @Test
    void factWithCommasSurvivesSerializeAndDeserialize() {
        CatFact original = new CatFact(9, "Cats, unlike dogs, cannot taste sweetness");

        String json = App.gson.toJson(original);
        CatFact result = App.gson.fromJson(json, CatFact.class);

        assertEquals(original, result);
    }

    @Test
    void loadFromCsvReadsEveryRow() throws IOException {
        App.loadFromCsv();

        assertEquals(10, App.facts.size());
    }

    @Test
    void factWithCommasIsStoredWhole() throws IOException {
        App.loadFromCsv();

        CatFact fact9 = null;
        for (CatFact f : App.facts) {
            if (f.id() == 9) {
                fact9 = f;
            }
        }

        assertEquals("Cats, like people, tend to favor one paw over the other", fact9.fact());
    }

    @Test
    void idsComeFromTheFile() throws IOException {
        App.loadFromCsv();

        assertEquals(1, App.facts.get(0).id());
    }

    @Test
    void nextIdIsOnePastHighestId() throws IOException {
        App.loadFromCsv();

        assertEquals(11, App.nextId);
    }

    // This fails: loadFromCsv just adds to the list without clearing it, so the second call gives 20 facts.
    // I would fix it by calling facts.clear() at the start of loadFromCsv.
    @Test
    void loadFromCsvTwiceDoesNotDoubleFacts() throws IOException {
        App.loadFromCsv();
        App.loadFromCsv();

        assertEquals(10, App.facts.size());
    }

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

        List<CatFact> facts = new App.FactsHandler().getAllFacts(url);

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

        Optional<CatFact> found = new App.FactsHandler().findFactById(url, 2);

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

        Optional<CatFact> found = new App.FactsHandler().findFactById(url, 99);

        assertFalse(found.isPresent());
    }
}
