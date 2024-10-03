package edu.cwru.ooa21.taskscheduler.models;

import edu.cwru.ooa21.taskscheduler.models.Duration;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class DurationTest {

    @Test
    void testOfMillis() {
        Duration duration = Duration.ofMillis(1000);
        assertNotNull(duration);
        assertEquals(BigInteger.valueOf(1000), duration.toMillis());
    }

    @Test
    void testAdd() {
        Duration duration1 = Duration.ofMillis(1000); // 1 second
        Duration duration2 = Duration.ofMillis(2000); // 2 seconds
        Duration result = duration1.add(duration2);

        assertEquals(BigInteger.valueOf(3000), result.toMillis()); // 1 + 2 = 3 seconds
    }

    @Test
    void testSubtract() {
        Duration duration1 = Duration.ofMillis(3000); // 3 seconds
        Duration duration2 = Duration.ofMillis(1000); // 1 second
        Duration result = duration1.subtract(duration2);

        assertEquals(BigInteger.valueOf(2000), result.toMillis()); // 3 - 1 = 2 seconds
    }

    @Test
    void testCompareTo() {
        Duration duration1 = Duration.ofMillis(1000); // 1 second
        Duration duration2 = Duration.ofMillis(2000); // 2 seconds

        assertTrue(duration1.compareTo(duration2) < 0); // 1 second is less than 2 seconds
        assertTrue(duration2.compareTo(duration1) > 0); // 2 seconds is greater than 1 second
        assertEquals(0, duration1.compareTo(Duration.ofMillis(1000))); // 1 second is equal to 1 second
    }

    @Test
    void testToMillis() {
        Duration duration = Duration.ofMillis(5000); // 5 seconds
        assertEquals(BigInteger.valueOf(5000), duration.toMillis());
    }
}
