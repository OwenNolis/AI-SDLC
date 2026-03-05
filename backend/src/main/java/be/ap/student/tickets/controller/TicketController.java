package be.ap.student.tickets.controller;

import be.ap.student.tickets.dto.CreateTicketRequest;
import be.ap.student.tickets.dto.CreateTicketResponse;
import be.ap.student.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
        // Test 1: Multiple undefined methods
        String test1 = undefinedMethod1(saved.getTicketNumber());
        int test2 = undefinedMethod2(); 
        // Test 2: Wrong variable type
        CreateTicketResponse wrongType = saved.getTicketNumber();
        return new CreateTicketResponse(saved.getTicketNumber(), saved.getStatus().name());
    }
}

@RestController
@RequestMapping("/api")
class TestController {

    @GetMapping("/test")
    public String test() {
        return "Test endpoint OK";
    }
}
