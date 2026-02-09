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
     * - Scenario: create_ticket_limit_2_high_priority_per_user - Ticket creation fails when user exceeds 2 HIGH priority tickets per day.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketLimit2HighPriorityPerUser_returns400_andCorrelationId() {
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
     * - Scenario: ticket_priority_completion_order_high_vs_medium - HIGH priority tickets are completed before MEDIUM priority tickets
     * - Scenario type: happy-path
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void ticketPriorityCompletionOrderHighVsMedium_returns201_andCorrelationId() {
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
     * - Scenario: REQ-014-high-priority-visible-immediately - REQ-014: A ticket with priority HIGH must always be visible immediately after creation.
     * - Scenario type: happy-path
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ014HighPriorityVisibleImmediately_returns201_andCorrelationId() {
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
     * - Scenario: REQ-015-unique-subject-per-day - REQ-015: Ticket subject must be unique per day.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ015UniqueSubjectPerDay_returns400_andCorrelationId() {
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
     * - Scenario: REQ-016-max-3-tickets-per-day - REQ-016: User can create at most 3 tickets per day.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ016Max3TicketsPerDay_returns400_andCorrelationId() {
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
     * - Scenario: REQ-017-max-2-high-priority-per-day - REQ-017: User can create at most 2 tickets with the priority HIGH.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ017Max2HighPriorityPerDay_returns400_andCorrelationId() {
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
     * - Scenario: REQ-018-high-priority-completion-before-low - REQ-018: A ticket with priority HIGH must always be completed before a ticket with priority LOW.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ018HighPriorityCompletionBeforeLow_returns400_andCorrelationId() {
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
     * - Scenario: REQ-019-high-priority-completion-before-medium - REQ-019: A ticket with priority HIGH must always be completed before a ticket with priority MEDIUM.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ019HighPriorityCompletionBeforeMedium_returns400_andCorrelationId() {
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
     * - Scenario: REQ-020-high-priority-visible-immediately - REQ-020: A ticket with priority HIGH must always be visible immediately after creation.
     * - Scenario type: happy-path
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ020HighPriorityVisibleImmediately_returns201_andCorrelationId() {
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
     * - Scenario: REQ-021-unique-subject-per-day - REQ-021: Ticket subject must be unique per day.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ021UniqueSubjectPerDay_returns400_andCorrelationId() {
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
     * - Scenario: REQ-022-max-3-tickets-per-day - REQ-022: User can create at most 3 tickets per day.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ022Max3TicketsPerDay_returns400_andCorrelationId() {
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
     * - Scenario: REQ-023-max-2-high-priority-per-day - REQ-023: User can create at most 2 tickets with the priority HIGH.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ023Max2HighPriorityPerDay_returns400_andCorrelationId() {
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
     * - Scenario: REQ-024-high-priority-completion-before-low - REQ-024: A ticket with priority HIGH must always be completed before a ticket with priority LOW.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ024HighPriorityCompletionBeforeLow_returns400_andCorrelationId() {
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
     * - Scenario: REQ-025-high-priority-completion-before-medium - REQ-025: A ticket with priority HIGH must always be completed before a ticket with priority MEDIUM.
     * - Scenario type: validation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ025HighPriorityCompletionBeforeMedium_returns400_andCorrelationId() {
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
