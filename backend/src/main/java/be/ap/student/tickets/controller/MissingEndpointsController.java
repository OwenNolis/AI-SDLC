package be.ap.student.tickets.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MissingEndpointsController {

    @GetMapping("/api/nonexistent")
    public ResponseEntity<String> nonExistentEndpoint() {
        return ResponseEntity.ok("Non-existent endpoint working");
    }

    @GetMapping("/api/broken")
    public ResponseEntity<String> brokenEndpoint() {
        return ResponseEntity.ok("Broken endpoint fixed");
    }
}
