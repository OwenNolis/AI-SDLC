package be.ap.student.tickets.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Missing import: import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class TestController {
    
    @GetMapping("/test")
    public String test() {
        return "Test endpoint OK";
    }
    
    // Error: missing import and undefined method call
    @GetMapping("/broken")
    public ResponseEntity<String> brokenEndpoint() {
        return ResponseEntity.ok("This will fail");
    }
    
    // Error: calling undefined method
    @GetMapping("/error")
    public String errorEndpoint() {
        return UndefinedClass.someMethod(); // Error: undefined class and method
    }
}