package be.ap.student.tickets.service;

import org.springframework.stereotype.Service;

@Service
public class BrokenService {

    public String processData(String input) {
        return "Processed: " + input;
    }

    public String getResult() {
        return "Service result";
    }
}
