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
        // Introduce an unused variable (code smell for SonarQube)
        String unused = "this variable is never used";
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
