package be.ap.student.tickets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused test that verifies the HTTP status codes returned by the ticket API.
 * <p>
 * This test is specifically designed to catch semantic errors like returning
 * 200 OK instead of 201 CREATED on the POST endpoint. Such errors don't cause
 * compilation failures — only test failures — which exercises the AI fix
 * pipeline's "test-only mode" (enhancement #7).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class TicketStatusCodeIT {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void createTicket_shouldReturn201Created() {
        Map<String, Object> payload = Map.of(
                "subject", "Test status code verification",
                "description", "This test ensures the create endpoint returns 201 CREATED, not 200 OK.",
                "priority", "LOW"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = rest.postForEntity("/api/tickets", request, String.class);

        assertThat(response.getStatusCode())
                .as("POST /api/tickets should return 201 CREATED for a valid ticket")
                .isEqualTo(HttpStatus.CREATED);
    }
}
