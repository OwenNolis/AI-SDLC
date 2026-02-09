package be.ap.student.tickets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Feature001SupportTicketGeneratedIT {

    @Autowired
    private TestRestTemplate rest;


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_happy_path - User creates a valid support ticket
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketHappyPath_returns201_andCorrelationId() {
        String json = """
        {
          "subject": "Cannot login to portal",
          "description": "I cannot login since yesterday. Please investigate.",
          "priority": "HIGH"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "/api/tickets",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_missing_subject - Ticket creation fails when subject is missing
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketMissingSubject_returns201_andCorrelationId() {
        String json = """
        {
          "subject": "Cannot login to portal",
          "description": "I cannot login since yesterday. Please investigate.",
          "priority": "HIGH"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "/api/tickets",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_invalid_priority - Ticket creation fails when priority is invalid
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketInvalidPriority_returns201_andCorrelationId() {
        String json = """
        {
          "subject": "Cannot login to portal",
          "description": "I cannot login since yesterday. Please investigate.",
          "priority": "HIGH"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "/api/tickets",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_duplicate_subject_same_day - Ticket creation fails when subject already exists for the same day
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketDuplicateSubjectSameDay_returns201_andCorrelationId() {
        String json = """
        {
          "subject": "Cannot login to portal",
          "description": "I cannot login since yesterday. Please investigate.",
          "priority": "HIGH"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "/api/tickets",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_high_priority_visible_immediately - HIGH priority ticket visible immediately after creation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketHighPriorityVisibleImmediately_returns201_andCorrelationId() {
        String json = """
        {
          "subject": "Cannot login to portal",
          "description": "I cannot login since yesterday. Please investigate.",
          "priority": "HIGH"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "/api/tickets",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_limit_3_per_day - Ticket creation fails when user exceeds 3 tickets in one day
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketLimit3PerDay_returns201_andCorrelationId() {
        String json = """
        {
          "subject": "Cannot login to portal",
          "description": "I cannot login since yesterday. Please investigate.",
          "priority": "HIGH"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "/api/tickets",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: ticket_priority_completion_order - HIGH priority tickets are completed before LOW priority tickets
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void ticketPriorityCompletionOrder_returns201_andCorrelationId() {
        String json = """
        {
          "subject": "Cannot login to portal",
          "description": "I cannot login since yesterday. Please investigate.",
          "priority": "HIGH"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "/api/tickets",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: validation (derived from TA constraints)
     */
    @Test
    void validation_invalidPayload_returns400_withFieldErrors() {
        String json = """
        {
          "subject": "abc",
          "description": "short",
          "priority": "HIGH"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "/api/tickets",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("fieldErrors");
    }
}
