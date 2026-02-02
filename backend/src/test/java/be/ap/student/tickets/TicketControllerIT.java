package be.ap.student.tickets;

import be.ap.student.tickets.dto.CreateTicketRequest;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TicketControllerIT {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void createTicket_returns201() {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setSubject("Cannot login to portal");
        req.setDescription("I cannot login since yesterday. Please investigate.");
        req.setPriority("HIGH");

        ResponseEntity<String> res = rest.postForEntity("/api/tickets", req, String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber").contains("OPEN");
    }

    @Test
    void createTicket_invalid_returns400_with_fieldErrors() {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setSubject("abc"); // too short
        req.setDescription("short"); // too short
        req.setPriority("HIGH");

        ResponseEntity<String> res = rest.postForEntity("/api/tickets", req, String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).contains("VALIDATION_ERROR").contains("fieldErrors");
    }
}