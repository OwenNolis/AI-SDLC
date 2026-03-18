package be.ap.student.tickets.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deliberate SonarQube MAJOR issues for testing the detailed PR comment feature.
 *
 * Triggered rules:
 *  - java:S106  (MAJOR) Standard outputs should not be used directly to log anything
 *  - java:S1192 (MAJOR) String literals should not be duplicated
 */
public class SonarIssueDemo {

    private static final String SERVICE_NAME = "ticket-service"; // Fix for java:S1192
    private static final Logger log = LoggerFactory.getLogger(SonarIssueDemo.class); // Fix for java:S106

    public String getServiceName() {
        return SERVICE_NAME;
    }

    public String getServiceId() {
        return SERVICE_NAME;
    }

    public String getServiceLabel() {
        return SERVICE_NAME;
    }

    public String getServiceTag() {
        return SERVICE_NAME;
    }

    public void reportStatus(String status) {
        log.info("Service status: {}", status); // Fix for java:S106
    }
}
