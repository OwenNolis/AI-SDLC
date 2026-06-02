package be.ap.student.tickets;

import be.ap.student.config.TestRestTemplateConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestRestTemplateConfig.class)
class Feature011PreworkoutWebsiteGeneratedIT {

    @Autowired
    private RestTemplate rest;

    @LocalServerPort
    private int port;

    private ResponseEntity<String> postTicket(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
        return rest.postForEntity("http://localhost:" + port + "/api/tickets", req, String.class);
    }


    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Scenario: create_ticket_happy_path - happy path
     * - Source: docs/test-scenarios/feature-011-preworkout-website.flow.json
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


    // ------------------------------------------------------------
    // TA MATRIX TESTS ENABLED (--matrix)
    // ------------------------------------------------------------


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.id -> missing
     */
    @Test
    void matrixUserIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.firstName -> empty
     */
    @Test
    void matrixUserFirstNameEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("firstName", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.firstName -> too_long
     */
    @Test
    void matrixUserFirstNameTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("firstName", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.firstName -> missing
     */
    @Test
    void matrixUserFirstNameMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.lastName -> empty
     */
    @Test
    void matrixUserLastNameEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("lastName", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.lastName -> too_long
     */
    @Test
    void matrixUserLastNameTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("lastName", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.lastName -> missing
     */
    @Test
    void matrixUserLastNameMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.email -> empty
     */
    @Test
    void matrixUserEmailEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("email", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.email -> too_long
     */
    @Test
    void matrixUserEmailTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("email", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.email -> missing
     */
    @Test
    void matrixUserEmailMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.email -> invalid_value
     */
    @Test
    void matrixUserEmailInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("email", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.email -> duplicate_per_day
     */
    @Test
    void matrixUserEmailDuplicatePerDay_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("email", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.passwordHash -> empty
     */
    @Test
    void matrixUserPasswordHashEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("passwordHash", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.passwordHash -> too_short
     */
    @Test
    void matrixUserPasswordHashTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("passwordHash", "aaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.passwordHash -> too_long
     */
    @Test
    void matrixUserPasswordHashTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("passwordHash", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.passwordHash -> missing
     */
    @Test
    void matrixUserPasswordHashMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.role -> missing
     */
    @Test
    void matrixUserRoleMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.role -> invalid_value
     */
    @Test
    void matrixUserRoleInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("role", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.createdAt -> missing
     */
    @Test
    void matrixUserCreatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: User.updatedAt -> missing
     */
    @Test
    void matrixUserUpdatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.id -> missing
     */
    @Test
    void matrixCartItemIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.userId -> missing
     */
    @Test
    void matrixCartItemUserIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.userId -> invalid_value
     */
    @Test
    void matrixCartItemUserIdInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("userId", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.productId -> missing
     */
    @Test
    void matrixCartItemProductIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.productId -> invalid_value
     */
    @Test
    void matrixCartItemProductIdInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("productId", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.quantity -> empty
     */
    @Test
    void matrixCartItemQuantityEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("quantity", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.quantity -> too_short
     */
    @Test
    void matrixCartItemQuantityTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("quantity", "aaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.quantity -> missing
     */
    @Test
    void matrixCartItemQuantityMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.quantity -> invalid_value
     */
    @Test
    void matrixCartItemQuantityInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("quantity", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.createdAt -> missing
     */
    @Test
    void matrixCartItemCreatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: CartItem.updatedAt -> missing
     */
    @Test
    void matrixCartItemUpdatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.id -> missing
     */
    @Test
    void matrixProductIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.name -> empty
     */
    @Test
    void matrixProductNameEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("name", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.name -> too_long
     */
    @Test
    void matrixProductNameTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.name -> missing
     */
    @Test
    void matrixProductNameMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.description -> empty
     */
    @Test
    void matrixProductDescriptionEmpty_rejected() {

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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.description -> too_long
     */
    @Test
    void matrixProductDescriptionTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.description -> missing
     */
    @Test
    void matrixProductDescriptionMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("priority", "HIGH");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.price -> empty
     */
    @Test
    void matrixProductPriceEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("price", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.price -> too_short
     */
    @Test
    void matrixProductPriceTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("price", "aaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.price -> missing
     */
    @Test
    void matrixProductPriceMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.price -> invalid_value
     */
    @Test
    void matrixProductPriceInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("price", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.flavor -> too_long
     */
    @Test
    void matrixProductFlavorTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("flavor", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.flavor -> invalid_value
     */
    @Test
    void matrixProductFlavorInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("flavor", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.caffeineMg -> too_short
     */
    @Test
    void matrixProductCaffeineMgTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("caffeineMg", "aaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.caffeineMg -> invalid_value
     */
    @Test
    void matrixProductCaffeineMgInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("caffeineMg", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.servings -> too_short
     */
    @Test
    void matrixProductServingsTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("servings", "aaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.servings -> invalid_value
     */
    @Test
    void matrixProductServingsInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("servings", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.stock -> empty
     */
    @Test
    void matrixProductStockEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("stock", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.stock -> too_short
     */
    @Test
    void matrixProductStockTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("stock", "aaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.stock -> missing
     */
    @Test
    void matrixProductStockMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.stock -> invalid_value
     */
    @Test
    void matrixProductStockInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("stock", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.imageUrl -> too_long
     */
    @Test
    void matrixProductImageUrlTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("imageUrl", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.imageUrl -> invalid_value
     */
    @Test
    void matrixProductImageUrlInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("imageUrl", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.isActive -> missing
     */
    @Test
    void matrixProductIsActiveMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.isActive -> invalid_value
     */
    @Test
    void matrixProductIsActiveInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("isActive", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.createdAt -> missing
     */
    @Test
    void matrixProductCreatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Product.updatedAt -> missing
     */
    @Test
    void matrixProductUpdatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.id -> missing
     */
    @Test
    void matrixOrderIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.userId -> missing
     */
    @Test
    void matrixOrderUserIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.userId -> invalid_value
     */
    @Test
    void matrixOrderUserIdInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("userId", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.totalPrice -> empty
     */
    @Test
    void matrixOrderTotalPriceEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("totalPrice", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.totalPrice -> too_short
     */
    @Test
    void matrixOrderTotalPriceTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("totalPrice", "aaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.totalPrice -> missing
     */
    @Test
    void matrixOrderTotalPriceMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.totalPrice -> invalid_value
     */
    @Test
    void matrixOrderTotalPriceInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("totalPrice", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.status -> missing
     */
    @Test
    void matrixOrderStatusMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.status -> invalid_value
     */
    @Test
    void matrixOrderStatusInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("status", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.shippingAddress -> empty
     */
    @Test
    void matrixOrderShippingAddressEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("shippingAddress", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.shippingAddress -> too_long
     */
    @Test
    void matrixOrderShippingAddressTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("shippingAddress", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.shippingAddress -> missing
     */
    @Test
    void matrixOrderShippingAddressMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.createdAt -> missing
     */
    @Test
    void matrixOrderCreatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Order.updatedAt -> missing
     */
    @Test
    void matrixOrderUpdatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.id -> missing
     */
    @Test
    void matrixOrderItemIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.orderId -> missing
     */
    @Test
    void matrixOrderItemOrderIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.orderId -> invalid_value
     */
    @Test
    void matrixOrderItemOrderIdInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("orderId", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.productId -> missing
     */
    @Test
    void matrixOrderItemProductIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.productId -> invalid_value
     */
    @Test
    void matrixOrderItemProductIdInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("productId", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.quantity -> empty
     */
    @Test
    void matrixOrderItemQuantityEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("quantity", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.quantity -> too_short
     */
    @Test
    void matrixOrderItemQuantityTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("quantity", "aaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.quantity -> missing
     */
    @Test
    void matrixOrderItemQuantityMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.quantity -> invalid_value
     */
    @Test
    void matrixOrderItemQuantityInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("quantity", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.unitPrice -> empty
     */
    @Test
    void matrixOrderItemUnitPriceEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("unitPrice", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.unitPrice -> too_short
     */
    @Test
    void matrixOrderItemUnitPriceTooShort_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("unitPrice", "aaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.unitPrice -> missing
     */
    @Test
    void matrixOrderItemUnitPriceMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.unitPrice -> invalid_value
     */
    @Test
    void matrixOrderItemUnitPriceInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("unitPrice", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.createdAt -> missing
     */
    @Test
    void matrixOrderItemCreatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderItem.updatedAt -> missing
     */
    @Test
    void matrixOrderItemUpdatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.id -> missing
     */
    @Test
    void matrixPaymentIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.orderId -> missing
     */
    @Test
    void matrixPaymentOrderIdMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.orderId -> invalid_value
     */
    @Test
    void matrixPaymentOrderIdInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("orderId", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.provider -> missing
     */
    @Test
    void matrixPaymentProviderMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.provider -> invalid_value
     */
    @Test
    void matrixPaymentProviderInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("provider", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.status -> missing
     */
    @Test
    void matrixPaymentStatusMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.status -> invalid_value
     */
    @Test
    void matrixPaymentStatusInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("status", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.transactionReference -> empty
     */
    @Test
    void matrixPaymentTransactionReferenceEmpty_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("transactionReference", "");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.transactionReference -> too_long
     */
    @Test
    void matrixPaymentTransactionReferenceTooLong_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("transactionReference", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.transactionReference -> missing
     */
    @Test
    void matrixPaymentTransactionReferenceMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.transactionReference -> invalid_value
     */
    @Test
    void matrixPaymentTransactionReferenceInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("transactionReference", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.transactionReference -> duplicate_per_day
     */
    @Test
    void matrixPaymentTransactionReferenceDuplicatePerDay_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("transactionReference", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.paidAt -> invalid_value
     */
    @Test
    void matrixPaymentPaidAtInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("paidAt", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.createdAt -> missing
     */
    @Test
    void matrixPaymentCreatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Payment.updatedAt -> missing
     */
    @Test
    void matrixPaymentUpdatedAtMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Role.value -> missing
     */
    @Test
    void matrixRoleValueMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: Role.value -> invalid_value
     */
    @Test
    void matrixRoleValueInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("value", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderStatus.value -> missing
     */
    @Test
    void matrixOrderStatusValueMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: OrderStatus.value -> invalid_value
     */
    @Test
    void matrixOrderStatusValueInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("value", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: PaymentProvider.value -> missing
     */
    @Test
    void matrixPaymentProviderValueMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: PaymentProvider.value -> invalid_value
     */
    @Test
    void matrixPaymentProviderValueInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("value", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }


    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: PaymentStatus.value -> missing
     */
    @Test
    void matrixPaymentStatusValueMissing_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
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
     * - Feature: feature-011-preworkout-website
     * - Source: docs/technical-analysis/feature-011-preworkout-website.ta.json
     * - Matrix: PaymentStatus.value -> invalid_value
     */
    @Test
    void matrixPaymentStatusValueInvalidValue_rejected() {

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("subject", "Cannot login to portal");
        payload.put("description", "I cannot login since yesterday. Please investigate.");
        payload.put("priority", "HIGH");
        payload.put("value", "INVALID");

        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 422);
        assertThat(res.getBody()).isNotNull();
    }

}
