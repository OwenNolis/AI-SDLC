package be.ap.student.tickets.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String testEndpoint() {
        return "Test endpoint works!";
    }

    // Removed duplicate /nonexistent and /broken endpoints to resolve ambiguous mapping.
    // These endpoints are already defined in MissingEndpointsController.
}
