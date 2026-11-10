import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorBodyTest {

    @Test
    void errorBodyGivesBackItsMessage() {
        ErrorBody errorBody = new ErrorBody("not found");

        assertEquals("not found", errorBody.error());
    }
}
