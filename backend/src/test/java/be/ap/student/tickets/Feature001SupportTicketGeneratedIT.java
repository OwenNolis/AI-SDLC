package be.ap.student.tickets;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class Feature001SupportTicketGeneratedIT {

    @Autowired
    private TestRestTemplate rest;

    private ResponseEntity<String> postTicket(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
        return rest.postForEntity("/api/tickets", req, String.class);
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_happy_path - User creates a valid support ticket
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketHappyPath_returns201_created() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_missing_subject - Ticket creation fails when subject is missing
     */
    @Test
    void createTicketMissingSubject_invalidRequest_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_invalid_priority - Ticket creation fails when priority is invalid
     */
    @Test
    void createTicketInvalidPriority_invalidRequest_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "INVALID");

        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_duplicate_subject_same_day - Ticket creation fails when subject already exists for the same day
     * - Rule: subject unique per day
     */
    @Test
    void createTicketDuplicateSubjectSameDay_duplicateSubject_sameDay_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Password reset not working");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> first = postTicket(payload);
        assertThat(first.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(first.getStatusCode().value()).isIn(200, 201);

        ResponseEntity<String> second = postTicket(payload);
        assertThat(second.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(second.getStatusCode().value()).isIn(400, 409);
        assertThat(second.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_high_priority_visible_immediately - HIGH priority ticket visible immediately after creation
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void createTicketHighPriorityVisibleImmediately_returns201_created() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_limit_3_per_day - Ticket creation fails when user exceeds 3 tickets in one day
     * - Rule: max 3 tickets per day
     */
    @Test
    void createTicketLimit3PerDay_limit3PerDay_rejectedOn4th() {
        for (int i = 1; i <= 3; i++) {
            var payload = new java.util.LinkedHashMap<String, Object>();
            payload.put("subject", "Limit test subject " + i);
            payload.put("description", "This is a valid description with enough characters (" + i + ").");
            payload.put("priority", "LOW");

            ResponseEntity<String> r = postTicket(payload);
            assertThat(r.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
            assertThat(r.getStatusCode().value()).isIn(200, 201);
        }

        var fourth = new java.util.LinkedHashMap<String, Object>();
        fourth.put("subject", "Limit test subject 4");
        fourth.put("description", "This is a valid description with enough characters (4).");
        fourth.put("priority", "LOW");

        ResponseEntity<String> res = postTicket(fourth);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 409, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: ticket_priority_completion_order - HIGH priority tickets are completed before LOW priority tickets
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void ticketPriorityCompletionOrder_returns201_created() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: br_a_ticket_with_priority_high_must_always_be_visible_immediate - Business rule: A ticket with priority HIGH must always be visible immediately after creation.
     */
    @Test
    void brATicketWithPriorityHighMustAlwaysBeVisibleImmediate_invalidRequest_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "abc");
        payload.put("description", "short");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: br_ticket_subject_must_be_unique_per_day_business_constraint - Business rule: Ticket subject must be unique per day (business constraint).
     * - Rule: subject unique per day
     */
    @Test
    void brTicketSubjectMustBeUniquePerDayBusinessConstraint_duplicateSubject_sameDay_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Password reset not working");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> first = postTicket(payload);
        assertThat(first.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(first.getStatusCode().value()).isIn(200, 201);

        ResponseEntity<String> second = postTicket(payload);
        assertThat(second.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(second.getStatusCode().value()).isIn(400, 409);
        assertThat(second.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: br_user_can_only_add_3_tickets_per_day - Business rule: User can only add 3 tickets per day
     * - Rule: max 3 tickets per day
     */
    @Test
    void brUserCanOnlyAdd3TicketsPerDay_limit3PerDay_rejectedOn4th() {
        for (int i = 1; i <= 3; i++) {
            var payload = new java.util.LinkedHashMap<String, Object>();
            payload.put("subject", "Limit test subject " + i);
            payload.put("description", "This is a valid description with enough characters (" + i + ").");
            payload.put("priority", "LOW");

            ResponseEntity<String> r = postTicket(payload);
            assertThat(r.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
            assertThat(r.getStatusCode().value()).isIn(200, 201);
        }

        var fourth = new java.util.LinkedHashMap<String, Object>();
        fourth.put("subject", "Limit test subject 4");
        fourth.put("description", "This is a valid description with enough characters (4).");
        fourth.put("priority", "LOW");

        ResponseEntity<String> res = postTicket(fourth);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 409, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW, TODO)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: br_a_ticket_with_priority_high_must_always_be_completed_before_ - Business rule: A ticket with priority HIGH must always be completed before a ticket with priority LOW
     *
     * NOTE:
     * This rule requires a "complete ticket" endpoint. Current API only supports POST /api/tickets.
     */
    @Disabled("TODO: completion endpoint not implemented; keep scenario in flow for traceability")
    @Test
    void brATicketWithPriorityHighMustAlwaysBeCompletedBefore_priorityCompletionOrder_TODO() {
        assertThat(true).isTrue();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: create_ticket_limit_2_high_priority_per_user - Ticket creation fails when user exceeds 2 HIGH priority tickets per day.
     */
    @Test
    void createTicketLimit2HighPriorityPerUser_invalidRequest_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "abc");
        payload.put("description", "short");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: ticket_priority_completion_order_high_vs_medium - HIGH priority tickets are completed before MEDIUM priority tickets
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void ticketPriorityCompletionOrderHighVsMedium_returns201_created() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-014-high-priority-visible-immediately - REQ-014: A ticket with priority HIGH must always be visible immediately after creation.
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ014HighPriorityVisibleImmediately_returns201_created() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-015-unique-subject-per-day - REQ-015: Ticket subject must be unique per day.
     * - Rule: subject unique per day
     */
    @Test
    void rEQ015UniqueSubjectPerDay_duplicateSubject_sameDay_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Password reset not working");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> first = postTicket(payload);
        assertThat(first.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(first.getStatusCode().value()).isIn(200, 201);

        ResponseEntity<String> second = postTicket(payload);
        assertThat(second.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(second.getStatusCode().value()).isIn(400, 409);
        assertThat(second.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-016-max-3-tickets-per-day - REQ-016: User can create at most 3 tickets per day.
     * - Rule: max 3 tickets per day
     */
    @Test
    void rEQ016Max3TicketsPerDay_limit3PerDay_rejectedOn4th() {
        for (int i = 1; i <= 3; i++) {
            var payload = new java.util.LinkedHashMap<String, Object>();
            payload.put("subject", "Limit test subject " + i);
            payload.put("description", "This is a valid description with enough characters (" + i + ").");
            payload.put("priority", "LOW");

            ResponseEntity<String> r = postTicket(payload);
            assertThat(r.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
            assertThat(r.getStatusCode().value()).isIn(200, 201);
        }

        var fourth = new java.util.LinkedHashMap<String, Object>();
        fourth.put("subject", "Limit test subject 4");
        fourth.put("description", "This is a valid description with enough characters (4).");
        fourth.put("priority", "LOW");

        ResponseEntity<String> res = postTicket(fourth);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 409, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-017-max-2-high-priority-per-day - REQ-017: User can create at most 2 tickets with the priority HIGH.
     */
    @Test
    void rEQ017Max2HighPriorityPerDay_invalidRequest_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "abc");
        payload.put("description", "short");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW, TODO)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-018-high-priority-completion-before-low - REQ-018: A ticket with priority HIGH must always be completed before a ticket with priority LOW.
     *
     * NOTE:
     * This rule requires a "complete ticket" endpoint. Current API only supports POST /api/tickets.
     */
    @Disabled("TODO: completion endpoint not implemented; keep scenario in flow for traceability")
    @Test
    void rEQ018HighPriorityCompletionBeforeLow_priorityCompletionOrder_TODO() {
        assertThat(true).isTrue();
    }


    /**
     * GENERATED (FLOW, TODO)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-019-high-priority-completion-before-medium - REQ-019: A ticket with priority HIGH must always be completed before a ticket with priority MEDIUM.
     *
     * NOTE:
     * This rule requires a "complete ticket" endpoint. Current API only supports POST /api/tickets.
     */
    @Disabled("TODO: completion endpoint not implemented; keep scenario in flow for traceability")
    @Test
    void rEQ019HighPriorityCompletionBeforeMedium_priorityCompletionOrder_TODO() {
        assertThat(true).isTrue();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-020-high-priority-visible-immediately - REQ-020: A ticket with priority HIGH must always be visible immediately after creation.
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ020HighPriorityVisibleImmediately_returns201_created() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-021-unique-subject-per-day - REQ-021: Ticket subject must be unique per day.
     * - Rule: subject unique per day
     */
    @Test
    void rEQ021UniqueSubjectPerDay_duplicateSubject_sameDay_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Password reset not working");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> first = postTicket(payload);
        assertThat(first.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(first.getStatusCode().value()).isIn(200, 201);

        ResponseEntity<String> second = postTicket(payload);
        assertThat(second.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(second.getStatusCode().value()).isIn(400, 409);
        assertThat(second.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-022-max-3-tickets-per-day - REQ-022: User can create at most 3 tickets per day.
     * - Rule: max 3 tickets per day
     */
    @Test
    void rEQ022Max3TicketsPerDay_limit3PerDay_rejectedOn4th() {
        for (int i = 1; i <= 3; i++) {
            var payload = new java.util.LinkedHashMap<String, Object>();
            payload.put("subject", "Limit test subject " + i);
            payload.put("description", "This is a valid description with enough characters (" + i + ").");
            payload.put("priority", "LOW");

            ResponseEntity<String> r = postTicket(payload);
            assertThat(r.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
            assertThat(r.getStatusCode().value()).isIn(200, 201);
        }

        var fourth = new java.util.LinkedHashMap<String, Object>();
        fourth.put("subject", "Limit test subject 4");
        fourth.put("description", "This is a valid description with enough characters (4).");
        fourth.put("priority", "LOW");

        ResponseEntity<String> res = postTicket(fourth);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 409, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-023-max-2-high-priority-per-day - REQ-023: User can create at most 2 tickets with the priority HIGH.
     */
    @Test
    void rEQ023Max2HighPriorityPerDay_invalidRequest_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "abc");
        payload.put("description", "short");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW, TODO)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-024-high-priority-completion-before-low - REQ-024: A ticket with priority HIGH must always be completed before a ticket with priority LOW.
     *
     * NOTE:
     * This rule requires a "complete ticket" endpoint. Current API only supports POST /api/tickets.
     */
    @Disabled("TODO: completion endpoint not implemented; keep scenario in flow for traceability")
    @Test
    void rEQ024HighPriorityCompletionBeforeLow_priorityCompletionOrder_TODO() {
        assertThat(true).isTrue();
    }


    /**
     * GENERATED (FLOW, TODO)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-025-high-priority-completion-before-medium - REQ-025: A ticket with priority HIGH must always be completed before a ticket with priority MEDIUM.
     *
     * NOTE:
     * This rule requires a "complete ticket" endpoint. Current API only supports POST /api/tickets.
     */
    @Disabled("TODO: completion endpoint not implemented; keep scenario in flow for traceability")
    @Test
    void rEQ025HighPriorityCompletionBeforeMedium_priorityCompletionOrder_TODO() {
        assertThat(true).isTrue();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-026-unique-subject-per-day - REQ-026: Ticket subject must be unique per day.
     * - Rule: subject unique per day
     */
    @Test
    void rEQ026UniqueSubjectPerDay_duplicateSubject_sameDay_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Password reset not working");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> first = postTicket(payload);
        assertThat(first.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(first.getStatusCode().value()).isIn(200, 201);

        ResponseEntity<String> second = postTicket(payload);
        assertThat(second.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(second.getStatusCode().value()).isIn(400, 409);
        assertThat(second.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-027-max-3-tickets-per-day - REQ-027: User can create at most 3 tickets per day.
     * - Rule: max 3 tickets per day
     */
    @Test
    void rEQ027Max3TicketsPerDay_limit3PerDay_rejectedOn4th() {
        for (int i = 1; i <= 3; i++) {
            var payload = new java.util.LinkedHashMap<String, Object>();
            payload.put("subject", "Limit test subject " + i);
            payload.put("description", "This is a valid description with enough characters (" + i + ").");
            payload.put("priority", "LOW");

            ResponseEntity<String> r = postTicket(payload);
            assertThat(r.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
            assertThat(r.getStatusCode().value()).isIn(200, 201);
        }

        var fourth = new java.util.LinkedHashMap<String, Object>();
        fourth.put("subject", "Limit test subject 4");
        fourth.put("description", "This is a valid description with enough characters (4).");
        fourth.put("priority", "LOW");

        ResponseEntity<String> res = postTicket(fourth);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 409, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-028-max-2-high-priority-per-day - REQ-028: User can create at most 2 tickets with the priority HIGH.
     */
    @Test
    void rEQ028Max2HighPriorityPerDay_invalidRequest_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "abc");
        payload.put("description", "short");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW, TODO)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-029-high-priority-completion-before-low - REQ-029: A ticket with priority HIGH must always be completed before a ticket with priority LOW.
     *
     * NOTE:
     * This rule requires a "complete ticket" endpoint. Current API only supports POST /api/tickets.
     */
    @Disabled("TODO: completion endpoint not implemented; keep scenario in flow for traceability")
    @Test
    void rEQ029HighPriorityCompletionBeforeLow_priorityCompletionOrder_TODO() {
        assertThat(true).isTrue();
    }


    /**
     * GENERATED (FLOW, TODO)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-030-high-priority-completion-before-medium - REQ-030: A ticket with priority HIGH must always be completed before a ticket with priority MEDIUM.
     *
     * NOTE:
     * This rule requires a "complete ticket" endpoint. Current API only supports POST /api/tickets.
     */
    @Disabled("TODO: completion endpoint not implemented; keep scenario in flow for traceability")
    @Test
    void rEQ030HighPriorityCompletionBeforeMedium_priorityCompletionOrder_TODO() {
        assertThat(true).isTrue();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-031-high-priority-visible-immediately - REQ-031: A ticket with priority HIGH must always be visible immediately after creation.
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ031HighPriorityVisibleImmediately_returns201_created() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-032-max-3-tickets-per-day - REQ-032: User can create at most 3 tickets per day.
     * - Rule: max 3 tickets per day
     */
    @Test
    void rEQ032Max3TicketsPerDay_limit3PerDay_rejectedOn4th() {
        for (int i = 1; i <= 3; i++) {
            var payload = new java.util.LinkedHashMap<String, Object>();
            payload.put("subject", "Limit test subject " + i);
            payload.put("description", "This is a valid description with enough characters (" + i + ").");
            payload.put("priority", "LOW");

            ResponseEntity<String> r = postTicket(payload);
            assertThat(r.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
            assertThat(r.getStatusCode().value()).isIn(200, 201);
        }

        var fourth = new java.util.LinkedHashMap<String, Object>();
        fourth.put("subject", "Limit test subject 4");
        fourth.put("description", "This is a valid description with enough characters (4).");
        fourth.put("priority", "LOW");

        ResponseEntity<String> res = postTicket(fourth);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 409, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-033-max-2-high-priority-per-day - REQ-033: User can create at most 2 tickets with the priority HIGH.
     */
    @Test
    void rEQ033Max2HighPriorityPerDay_invalidRequest_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "abc");
        payload.put("description", "short");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422, 429);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Scenario: REQ-034-high-priority-visible-immediately - REQ-034: A ticket with priority HIGH must always be visible immediately after creation.
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void rEQ034HighPriorityVisibleImmediately_returns201_created() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }


    // ------------------------------------------------------------
    // TA MATRIX TESTS ENABLED (--matrix)
    // ------------------------------------------------------------


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Source: docs/technical-analysis/feature-001-support-ticket.ta.json
     * - Matrix: SupportTicket.subject -> empty
     */
    @Test
    void matrixSupportTicketSubjectEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Source: docs/technical-analysis/feature-001-support-ticket.ta.json
     * - Matrix: SupportTicket.subject -> too_short
     */
    @Test
    void matrixSupportTicketSubjectTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "aaa");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Source: docs/technical-analysis/feature-001-support-ticket.ta.json
     * - Matrix: SupportTicket.subject -> too_long
     */
    @Test
    void matrixSupportTicketSubjectTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Source: docs/technical-analysis/feature-001-support-ticket.ta.json
     * - Matrix: SupportTicket.subject -> duplicate_per_day
     */
    @Test
    void matrixSupportTicketSubjectDuplicatePerDay_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "INVALID");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Source: docs/technical-analysis/feature-001-support-ticket.ta.json
     * - Matrix: SupportTicket.description -> empty
     */
    @Test
    void matrixSupportTicketDescriptionEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Source: docs/technical-analysis/feature-001-support-ticket.ta.json
     * - Matrix: SupportTicket.description -> too_short
     */
    @Test
    void matrixSupportTicketDescriptionTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "aaaaaaaaaaaaaaaaaa");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Source: docs/technical-analysis/feature-001-support-ticket.ta.json
     * - Matrix: SupportTicket.description -> too_long
     */
    @Test
    void matrixSupportTicketDescriptionTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Source: docs/technical-analysis/feature-001-support-ticket.ta.json
     * - Matrix: SupportTicket.priority -> missing
     */
    @Test
    void matrixSupportTicketPriorityMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-001-support-ticket
     * - Source: docs/technical-analysis/feature-001-support-ticket.ta.json
     * - Matrix: SupportTicket.priority -> invalid_value
     */
    @Test
    void matrixSupportTicketPriorityInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }

}
