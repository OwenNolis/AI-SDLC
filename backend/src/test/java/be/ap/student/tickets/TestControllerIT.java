package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.resttestclient.AutoConfigureTestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class TestControllerIT {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testEndpoint() {
        String result = restTemplate.getForObject("/api/test", String.class);
        // Test should now pass with correct imports
    }
}
