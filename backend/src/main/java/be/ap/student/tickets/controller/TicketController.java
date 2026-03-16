    // This method introduces a SonarQube MAJOR issue: empty catch block (java:S108)
    public void triggerMajorSonarIssue() {
        try {
            int x = 1 / 0;
        } catch (Exception e) {
            // empty catch block - SonarQube will flag this as MAJOR
        }
    }
package be.ap.student.tickets.controller;

import be.ap.student.tickets.domain.SupportTicket;
import be.ap.student.tickets.domain.TicketStatus;
import be.ap.student.tickets.dto.CreateTicketRequest;
import be.ap.student.tickets.dto.CreateTicketResponse;
import be.ap.student.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTicketResponse create(@Valid @RequestBody CreateTicketRequest req) {
        var optionalSavedTicket = ticketService.create(req);
        SupportTicket saved = optionalSavedTicket.orElseThrow(() -> new IllegalStateException("Failed to create ticket"));
        // TODO: This is an unnecessary comment and will be flagged as a MINOR or INFO issue by SonarQube
        return new CreateTicketResponse(saved.getTicketNumber(), TicketStatus.valueOf(saved.getFormattedStatus()));
    }

    @GetMapping("/all")
    public List<String> getAllTickets() {
        return List.of("ticket1", "ticket2");
    }

    @GetMapping("/{id}")
    public CreateTicketResponse getById(@PathVariable UUID id) {
        var ticket = ticketService.findById(id);
        return new CreateTicketResponse(ticket.getTicketNumber(), TicketStatus.valueOf(ticket.getFormattedStatus()));
    }
}
