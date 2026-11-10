import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatFactTest {

    @Test
    void catFactKeepsIdAndFactPassedIn() {
        CatFact catFact = new CatFact(5, "Cats sleep a lot");

        assertEquals(5, catFact.id());
        assertEquals("Cats sleep a lot", catFact.fact());
    }

    @Test
    void twoCatFactsWithSameValuesAreEqual() {
        CatFact first = new CatFact(3, "A group of cats is called a clowder");
        CatFact second = new CatFact(3, "A group of cats is called a clowder");

        assertEquals(first, second);
    }

    @Test
    void twoCatFactsWithDifferentTextAreNotEqual() {
        CatFact first = new CatFact(3, "A group of cats is called a clowder");
        CatFact second = new CatFact(3, "Cats were worshipped in ancient Egypt");

        assertNotEquals(first, second);
    }

    @Test
    void listFindsCatFactByValues() {
        List<CatFact> list = new ArrayList<>();
        list.add(new CatFact(1, "A cat has 230 bones"));

        boolean found = list.contains(new CatFact(1, "A cat has 230 bones"));

        assertTrue(found);
    }

    // The record itself doesn't check for blank text, that only happens in the schema and the POST handler.
    @Test
    void catFactCanHoldEmptyText() {
        CatFact catFact = new CatFact(1, "");

        assertNotEquals(null, catFact);
        assertEquals("", catFact.fact());
    }
}
