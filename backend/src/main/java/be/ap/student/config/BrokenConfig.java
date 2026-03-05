package be.ap.student.config;

// Test 4: Configuration class errors
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class BrokenConfig {
    
    // Test: Bean method with compilation error
    @Bean
    public UndefinedClass brokenBean() {
        // Test: Creating instance of undefined class
        return new UndefinedClass("test", nonExistentVariable);
    }
    
    // Test: Wrong annotation usage
    @Bean
    public String stringBean() {
        // Test: Using undefined static method
        return StaticUtility.nonExistentMethod();
    }
    
    // Test: Generic type errors  
    @Bean
    public List<UndefinedGenericType> genericError() {
        return new ArrayList<>(); // Missing import for ArrayList
    }
}