import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    @Test
    void serializedCatFactContainsBothFields() {
        CatFact catFact = new CatFact(7, "Cats have five toes in front");

        String json = Json.gson.toJson(catFact);

        assertTrue(json.contains("\"id\": 7"));
        assertTrue(json.contains("Cats have five toes in front"));
    }

    @Test
    void jsonBodyDeserializesIntoNewFact() {
        String json = "{\"fact\": \"Cats can jump really high\"}";

        NewFact newFact = Json.gson.fromJson(json, NewFact.class);

        assertEquals("Cats can jump really high", newFact.fact());
    }

    @Test
    void apostropheStaysAnApostrophe() {
        CatFact catFact = new CatFact(2, "A cat's nose is unique");

        String json = Json.gson.toJson(catFact);

        assertTrue(json.contains("cat's"));
        assertFalse(json.contains("\\u0027"));
    }

    @Test
    void factWithCommasSurvivesSerializeAndDeserialize() {
        CatFact original = new CatFact(9, "Cats, unlike dogs, cannot taste sweetness");

        String json = Json.gson.toJson(original);
        CatFact result = Json.gson.fromJson(json, CatFact.class);

        assertEquals(original, result);
    }
}
