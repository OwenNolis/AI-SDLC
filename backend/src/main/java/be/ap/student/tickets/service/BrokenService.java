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
}
