package be.ap.student.tickets.controller;

import be.ap.student.tickets.domain.SupportTicket;
import be.ap.student.tickets.dto.CreateTicketRequest;
import be.ap.student.tickets.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<SupportTicket> createTicket(@RequestBody CreateTicketRequest request) {
        Optional<SupportTicket> createdTicket = ticketService.create(request);
        return createdTicket
                .map(ticket -> ResponseEntity.status(HttpStatus.CREATED).body(ticket))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build()); // Or another appropriate error status if creation fails
    }
}
