package be.ap.student.tickets.controller;

import be.ap.student.tickets.domain.SupportTicket;
import be.ap.student.tickets.domain.TicketStatus; // Added import
import be.ap.student.tickets.dto.CreateTicketRequest;
import be.ap.student.tickets.dto.CreateTicketResponse; // Added import
import be.ap.student.tickets.service.TicketService;
import jakarta.validation.Valid; // Added import for @Valid
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Added import
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTicketResponse create(@Valid @RequestBody CreateTicketRequest req) {
        // Removed service.validate(req); as TicketService does not have this method and @Valid annotation handles validation.
        var optionalSavedTicket = ticketService.create(req); // Corrected variable name from 'service' to 'ticketService'
        // Unwrap the Optional to access SupportTicket methods
        SupportTicket saved = optionalSavedTicket.orElseThrow(() -> new IllegalStateException("Failed to create ticket"));
        // Fix: Convert String from getFormattedStatus() to TicketStatus enum
        return new CreateTicketResponse(saved.getTicketNumber(), TicketStatus.valueOf(saved.getFormattedStatus()));
    }

    @GetMapping("/all")
    public List<String> getAllTickets() {
        // The current implementation returns hardcoded values. This is kept as is to fix compilation.
        // A more complete implementation would involve calling the service, e.g., ticketService.getAllTickets().stream().map(SupportTicket::getTicketNumber).collect(Collectors.toList());
        return List.of("ticket1", "ticket2");
    }

    @GetMapping("/{id}")
    public CreateTicketResponse getById(@PathVariable java.util.UUID id) { // Removed redundant package prefix for @PathVariable
        var ticket = ticketService.findById(id); // Corrected variable name from 'service' to 'ticketService'
        // Changed ticket.getStatus().name() to ticket.getFormattedStatus() for consistency with create method and to resolve 'cannot find symbol name()' error.
        // Fix: Convert String from getFormattedStatus() to TicketStatus enum
        return new CreateTicketResponse(ticket.getTicketNumber(), TicketStatus.valueOf(ticket.getFormattedStatus()));
    }
}
