package be.ap.student.tickets.service;

import org.springframework.stereotype.Service;

@Service
public class BrokenService {
    
    // Test 3: Method signature errors
    public String processData(InvalidType param) {
        // Test: Calling non-existent method
        String result = nonExistentUtility.process(param);
        
        // Test: Wrong return type
        return 12345; // Should return String, not int
    }
    
    // Test: Missing dependency injection
    private UndefinedRepository repository; // Should be @Autowired
    
    public void brokenMethod() {
        // Test: Using uninitialized field
        repository.save("test");
    }
}