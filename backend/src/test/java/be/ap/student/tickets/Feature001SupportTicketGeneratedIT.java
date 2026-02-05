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
     * - Scenario: scenario_1 - happy path
     * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
     */
    @Test
    void scenario1_returns201_andCorrelationId() {
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
