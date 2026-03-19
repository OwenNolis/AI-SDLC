package be.ap.student.tickets.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckDemoTest {

    private final HealthCheckDemo demo = new HealthCheckDemo();

    @Test
    void checkHealth_returnsOk() {
        assertEquals("OK: payments", demo.checkHealth("payments"));
    }

    @Test
    void checkHealth_nullThrows() {
        assertThrows(IllegalArgumentException.class, () -> demo.checkHealth(null));
    }

    @Test
    void checkHealth_blankThrows() {
        assertThrows(IllegalArgumentException.class, () -> demo.checkHealth("  "));
    }
}
