/*
 * Note: everything passes except loadFromCsvTwiceDoesNotDoubleFacts (question 17).
 * loadFromCsv never clears MigrateCsvToDb.facts before reading, so calling it twice gives 20 facts instead of 10.
 * Since facts is static it also carries over between tests, so I reset it in @BeforeEach
 * to keep the other loadFromCsv tests from depending on which order they run in.
 */
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MigrateCsvToDbTest {

    @BeforeEach
    void resetFacts() {
        MigrateCsvToDb.facts.clear();
        MigrateCsvToDb.nextId = 1;
    }

    @Test
    void loadFromCsvReadsEveryRow() throws IOException {
        MigrateCsvToDb.loadFromCsv();

        assertEquals(10, MigrateCsvToDb.facts.size());
    }

    @Test
    void factWithCommasIsStoredWhole() throws IOException {
        MigrateCsvToDb.loadFromCsv();

        CatFact fact9 = null;
        for (CatFact f : MigrateCsvToDb.facts) {
            if (f.id() == 9) {
                fact9 = f;
            }
        }

        assertEquals("Cats, like people, tend to favor one paw over the other", fact9.fact());
    }

    @Test
    void idsComeFromTheFile() throws IOException {
        MigrateCsvToDb.loadFromCsv();

        assertEquals(1, MigrateCsvToDb.facts.get(0).id());
    }

    @Test
    void nextIdIsOnePastHighestId() throws IOException {
        MigrateCsvToDb.loadFromCsv();

        assertEquals(11, MigrateCsvToDb.nextId);
    }

    // This fails: loadFromCsv just adds to the list without clearing it, so the second call gives 20 facts.
    // I would fix it by calling facts.clear() at the start of loadFromCsv.
    @Test
    void loadFromCsvTwiceDoesNotDoubleFacts() throws IOException {
        MigrateCsvToDb.loadFromCsv();
        MigrateCsvToDb.loadFromCsv();

        assertEquals(10, MigrateCsvToDb.facts.size());
    }
}
