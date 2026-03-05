package be.ap.student.tickets.controller;

// Test 2: Missing imports should trigger AI fixes
import java.util.List;
// Missing: import org.springframework.web.bind.annotation.*;
// Missing: import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/test-errors")
public class TestErrorController {
    
    // Test: Using annotations without proper imports
    @GetMapping("/missing-imports")
    public ResponseEntity<List<String>> testMissingImports() {
        // Test: Using undefined class
        UndefinedClass obj = new UndefinedClass();
        return ResponseEntity.ok(List.of("test"));
    }
    
    // Test: Wrong annotation usage
    @PostMapping("/syntax-error")
    public String testSyntaxError(
        // Missing @RequestBody annotation
        String invalidParam
    ) {
        return "error test";
    }
}