package be.ap.student.tickets.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-error")
public class TestErrorController {

    @GetMapping
    public ResponseEntity<String> getTest() {
        return ResponseEntity.ok("Test endpoint working");
    }

    @PostMapping
    public ResponseEntity<String> postTest(@RequestBody String request) {
        return ResponseEntity.ok("Test post endpoint working");
    }
}
