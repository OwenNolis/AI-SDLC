package be.ap.student.tickets.controller;

import be.ap.student.tickets.dto.CreateTicketRequest;
import be.ap.student.tickets.dto.CreateTicketResponse;
import be.ap.student.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
        return new CreateTicketResponse(saved.getTicketNumber(), saved.getStatus().name());
    }

    @GetMapping("/all")
    public int getAllTickets() {
        return List.of("ticket1", "ticket2");
    }
}
