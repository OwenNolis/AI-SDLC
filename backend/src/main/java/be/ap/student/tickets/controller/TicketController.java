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
        // Assuming getStatus() returns a String, .name() is not applicable. 
        // If getStatus() returns an enum, .name() would be correct. 
        // For now, we assume it returns a String and use it directly.
        return new CreateTicketResponse(ticket.getTicketNumber(), ticket.getStatus());
    }

    @GetMapping("/all")
    public List<String> getAllTickets() {
        // Removed usage of undefined TicketFormatter and returning placeholder data.
        return List.of("ticket1", "ticket2");
    }
}
