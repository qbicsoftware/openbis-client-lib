package life.qbic.openbis.openbisclient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PassingTest {
    @Test
    @DisplayName("this test is run")
    void thisTestIsRun() {
        assertEquals(1, 5-4);
    }

}
