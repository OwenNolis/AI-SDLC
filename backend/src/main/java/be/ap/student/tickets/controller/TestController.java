package be.ap.student.tickets.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public ResponseEntity<String> testEndpoint(HttpServletRequest request) {
        return ResponseEntity.ok("Test endpoint working");
    }

    @PostMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is healthy");
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        // Removed usage of undefined StatusChecker
        return ResponseEntity.ok("Status check not available");
    }
}
