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
     * - Scenario type: happy-path
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
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketMissingSubject_returns400_andCorrelationId() {
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


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_invalid_priority - Ticket creation fails when priority is invalid
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketInvalidPriority_returns400_andCorrelationId() {
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


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_duplicate_subject_same_day - Ticket creation fails when subject already exists for the same day
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketDuplicateSubjectSameDay_returns400_andCorrelationId() {
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


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_high_priority_visible_immediately - HIGH priority ticket visible immediately after creation
     * - Scenario type: happy-path
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
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketLimit3PerDay_returns400_andCorrelationId() {
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


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: ticket_priority_completion_order - HIGH priority tickets are completed before LOW priority tickets
     * - Scenario type: happy-path
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
     * - Scenario: br_a_ticket_with_priority_high_must_always_be_visible_immediate - Business rule: A ticket with priority HIGH must always be visible immediately after creation.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void brATicketWithPriorityHighMustAlwaysBeVisibleImmediate_returns400_andCorrelationId() {
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


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: br_ticket_subject_must_be_unique_per_day_business_constraint - Business rule: Ticket subject must be unique per day (business constraint).
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void brTicketSubjectMustBeUniquePerDayBusinessConstraint_returns400_andCorrelationId() {
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


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: br_user_can_only_add_3_tickets_per_day - Business rule: User can only add 3 tickets per day
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void brUserCanOnlyAdd3TicketsPerDay_returns400_andCorrelationId() {
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


    /**
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: br_a_ticket_with_priority_high_must_always_be_completed_before_ - Business rule: A ticket with priority HIGH must always be completed before a ticket with priority LOW
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void brATicketWithPriorityHighMustAlwaysBeCompletedBefore_returns400_andCorrelationId() {
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
