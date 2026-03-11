package be.ap.student.tickets;

import be.ap.student.config.TestRestTemplateConfig;
import be.ap.student.tickets.dto.CreateTicketRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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
@Import(TestRestTemplateConfig.class)
class TicketStatusCodeTest {

    @Autowired
    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void createTicket_shouldReturn201Created() {
        CreateTicketRequest payload = new CreateTicketRequest();
        payload.setSubject("Test status code verification");
        payload.setDescription("This test ensures the create endpoint returns 201 CREATED, not 200 OK.");
        payload.setPriority("LOW");
        payload.setUserId("00000000-0000-0000-0000-000000000001");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTicketRequest> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/tickets", request, String.class);

        assertThat(response.getStatusCode())
                .as("POST /api/tickets should return 201 CREATED for a valid ticket")
                .isEqualTo(HttpStatus.CREATED);
    }
}
