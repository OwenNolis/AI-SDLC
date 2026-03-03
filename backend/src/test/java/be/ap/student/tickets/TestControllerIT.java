package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
<<<<<<< ai-fix/auto-fixes-20260303-093139
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
=======
import org.springframework.context.annotation.Import;
import be.ap.student.config.TestRestTemplateConfig;
>>>>>>> main
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
<<<<<<< ai-fix/auto-fixes-20260303-093139
@AutoConfigureTestRestTemplate
=======
@Import(TestRestTemplateConfig.class)
>>>>>>> main
public class TestControllerIT {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testEndpoint() {
        String result = restTemplate.getForObject("/api/test", String.class);
        // Test should now pass with correct imports
    }
}
