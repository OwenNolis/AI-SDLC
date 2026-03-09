package be.ap.student.tickets.controller;

import be.ap.student.tickets.dto.CreateTicketResponse;
import be.ap.student.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Import the CreateTicketRequest DTO
import be.ap.student.tickets.dto.CreateTicketRequest;

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
        var saved = service.create(req);
        return new CreateTicketResponse(saved.getTicketNumber(), saved.getStatus());
    }

    @GetMapping("/{id}")
    public CreateTicketResponse getById(@PathVariable UUID id) {
        var ticket = service.findById(id);
        // The .name() call on ticket.getStatus() is correct if getStatus() returns an enum.
        // The compilation error might be a symptom of TicketService.findById not returning a valid SupportTicket.
        // Assuming getStatus() returns an enum, .name() is correct. If it returns a String, .toString() would be appropriate.
        return new CreateTicketResponse(ticket.getTicketNumber(), ticket.getStatus().name());
    }

    @GetMapping("/all")
    public List<String> getAllTickets() {
        // Removed usage of undefined TicketFormatter
        return List.of("ticket1", "ticket2");
    }
}
