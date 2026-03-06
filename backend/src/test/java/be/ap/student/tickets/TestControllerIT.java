package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import be.ap.student.config.TestRestTemplateConfig;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestRestTemplateConfig.class)
public class TestControllerIT {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @LocalServerPort
    private int port;
    
    @Test
    public void testEndpoint() {
        String result = restTemplate.getForObject("http://localhost:" + port + "/api/test", String.class);
        System.out.println("RestTemplate injected: " + (restTemplate != null));
        System.out.println("Test endpoint result: " + result);
    }
    
    // Error: Test will fail because /api/nonexistent doesn't exist
    @Test
    public void testNonExistentEndpoint() {
        String result = restTemplate.getForObject("http://localhost:" + port + "/api/nonexistent", String.class);
        System.out.println("This should fail: " + result);
    }
    
    // Error: Test will fail due to compilation error in controller
    @Test  
    public void testBrokenEndpoint() {
        String result = restTemplate.getForObject("http://localhost:" + port + "/api/broken", String.class);
        System.out.println("Broken endpoint result: " + result);
    }
}
