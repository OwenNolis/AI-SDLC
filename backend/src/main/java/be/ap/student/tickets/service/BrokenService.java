package be.ap.student.tickets.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BrokenService {

    public String processData(String input) {
        return "Processed: " + input;
    }

    public List<String> getItems() {
        return List.of("item1", "item2");
    }

    public String handleRequest(String parameter) {
        return "Handled: " + parameter;
    }

    public String validateInput(String data) {
        // Assuming DataTransformer is a static utility class. 
        // If it's a separate class, it needs to be imported and instantiated.
        // For now, replacing with a simple transformation.
        return "Transformed: " + data;
    }
}
