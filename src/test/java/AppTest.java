import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void catFactKeepsIdAndFactPassedIn() {
        App.CatFact catFact = new App.CatFact(5, "Cats sleep a lot");

        assertEquals(5, catFact.id());
        assertEquals("Cats sleep a lot", catFact.fact());
    }

    @Test
    void twoCatFactsWithSameValuesAreEqual() {
        App.CatFact first = new App.CatFact(3, "A group of cats is called a clowder");
        App.CatFact second = new App.CatFact(3, "A group of cats is called a clowder");

        assertEquals(first, second);
    }

    @Test
    void twoCatFactsWithDifferentTextAreNotEqual() {
        App.CatFact first = new App.CatFact(3, "A group of cats is called a clowder");
        App.CatFact second = new App.CatFact(3, "Cats were worshipped in ancient Egypt");

        assertNotEquals(first, second);
    }

    @Test
    void listFindsCatFactByValues() {
        List<App.CatFact> list = new ArrayList<>();
        list.add(new App.CatFact(1, "A cat has 230 bones"));

        boolean found = list.contains(new App.CatFact(1, "A cat has 230 bones"));

        assertTrue(found);
    }

    @Test
    void newFactGivesBackItsText() {
        App.NewFact newFact = new App.NewFact("Cats have whiskers");

        assertEquals("Cats have whiskers", newFact.fact());
    }

    @Test
    void errorBodyGivesBackItsMessage() {
        App.ErrorBody errorBody = new App.ErrorBody("not found");

        assertEquals("not found", errorBody.error());
    }

    // The record itself doesn't check for blank text, that only happens in the schema and the POST handler.
    @Test
    void catFactCanHoldEmptyText() {
        App.CatFact catFact = new App.CatFact(1, "");

        assertNotEquals(null, catFact);
        assertEquals("", catFact.fact());
    }

    @Test
    void serializedCatFactContainsBothFields() {
        App.CatFact catFact = new App.CatFact(7, "Cats have five toes in front");

        String json = App.gson.toJson(catFact);

        assertTrue(json.contains("\"id\": 7"));
        assertTrue(json.contains("Cats have five toes in front"));
    }

    @Test
    void jsonBodyDeserializesIntoNewFact() {
        String json = "{\"fact\": \"Cats can jump really high\"}";

        App.NewFact newFact = App.gson.fromJson(json, App.NewFact.class);

        assertEquals("Cats can jump really high", newFact.fact());
    }

    @Test
    void apostropheStaysAnApostrophe() {
        App.CatFact catFact = new App.CatFact(2, "A cat's nose is unique");

        String json = App.gson.toJson(catFact);

        assertTrue(json.contains("cat's"));
        assertFalse(json.contains("\\u0027"));
    }

    @Test
    void factWithCommasSurvivesSerializeAndDeserialize() {
        App.CatFact original = new App.CatFact(9, "Cats, unlike dogs, cannot taste sweetness");

        String json = App.gson.toJson(original);
        App.CatFact result = App.gson.fromJson(json, App.CatFact.class);

        assertEquals(original, result);
    }
}
