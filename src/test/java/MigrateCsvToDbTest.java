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

    // fails: loadFromCsv doesn't clear facts first, so calling it twice gives 20
    @Test
    void loadFromCsvTwiceDoesNotDoubleFacts() throws IOException {
        MigrateCsvToDb.loadFromCsv();
        MigrateCsvToDb.loadFromCsv();

        assertEquals(10, MigrateCsvToDb.facts.size());
    }
}
