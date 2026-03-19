package be.ap.student.tickets.controller;

/**
 * Deliberate SonarQube MEDIUM issue for testing the detailed PR comment feature.
 *
 * Triggered rule:
 *  - java:S112 (MEDIUM) Generic exceptions RuntimeException, Exception and Error
 *    should not be thrown — throw a more specific exception type instead.
 */
public class HealthCheckDemo {

    // java:S112 — RuntimeException is too generic; should use a specific subclass
    public String checkHealth(String component) {
        if (component == null || component.isBlank()) {
            throw new RuntimeException("Component name must not be blank");
        }
        return "OK: " + component;
    }
}
