package be.ap.student.tickets.controller;

/**
 * Deliberate SonarQube MAJOR issues for testing the detailed PR comment feature.
 *
 * Triggered rules:
 *  - java:S106  (MAJOR) Standard outputs should not be used directly to log anything
 *  - java:S1192 (MAJOR) String literals should not be duplicated
 */
public class SonarIssueDemo {

    // java:S1192 — "ticket-service" repeated 4 times (threshold is 3)
    public String getServiceName() {
        return "ticket-service";
    }

    public String getServiceId() {
        return "ticket-service";
    }

    public String getServiceLabel() {
        return "ticket-service";
    }

    public String getServiceTag() {
        return "ticket-service";
    }

    // java:S106 — System.out used instead of a proper logger
    public void reportStatus(String status) {
        System.out.println("Service status: " + status);
    }
}
