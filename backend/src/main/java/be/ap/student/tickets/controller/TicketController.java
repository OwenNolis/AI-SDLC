package be.ap.student.tickets.controller;

import be.ap.student.tickets.dto.CreateTicketRequest;
import be.ap.student.tickets.dto.CreateTicketResponse;
import be.ap.student.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import be.ap.student.tickets.domain.SupportTicket; // Added import for SupportTicket

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTicketResponse create(@Valid @RequestBody CreateTicketRequest req) {
        // Removed service.validate(req); as TicketService does not have this method and @Valid annotation handles validation.
        var optionalSavedTicket = service.create(req);
        // Unwrap the Optional to access SupportTicket methods
        SupportTicket saved = optionalSavedTicket.orElseThrow(() -> new IllegalStateException("Failed to create ticket"));
        return new CreateTicketResponse(saved.getTicketNumber(), saved.getFormattedStatus());
    }

    @GetMapping("/all")
    public List<String> getAllTickets() {
        return List.of("ticket1", "ticket2");
    }

    @GetMapping("/{id}")
    public CreateTicketResponse getById(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id) {
        var ticket = service.findById(id);
        return new CreateTicketResponse(ticket.getTicketNumber(), ticket.getStatus().name());
    }
}
