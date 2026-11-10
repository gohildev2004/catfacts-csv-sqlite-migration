import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NewFactTest {

    @Test
    void newFactGivesBackItsText() {
        NewFact newFact = new NewFact("Cats have whiskers");

        assertEquals("Cats have whiskers", newFact.fact());
    }
}
