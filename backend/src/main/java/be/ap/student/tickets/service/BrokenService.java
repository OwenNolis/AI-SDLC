package be.ap.student.tickets.service;

import org.springframework.stereotype.Service;
// Missing import: import java.util.List;

@Service
public class BrokenService {

    // Error: undefined return type
    public UndefinedType processData(String input) {
        return new UndefinedType(input); // Error: undefined class
    }
    
    // Error: missing import for List
    public List<String> getResults() {
        MissingClass helper = new MissingClass(); // Error: undefined class
        return helper.getList(); // Error: undefined method
    }
    
    // Error: incorrect method signature
    public void invalidMethod(NonExistentParameter param) {
        param.doSomething(); // Error: undefined type and method
    }
}
