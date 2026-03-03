package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import be.ap.student.config.TestRestTemplateConfig;
import org.springframework.context.annotation.Import;
import be.ap.student.config.RestTemplateConfig;
import org.springframework.context.annotation.Import;
import be.ap.student.config.RestTemplateConfig;
import org.springframework.context.annotation.Import;
import be.ap.student.config.RestTemplateConfig;
import org.springframework.context.annotation.Import;
import be.ap.student.config.RestTemplateConfig;
import org.springframework.context.annotation.Import;
import be.ap.student.config.RestTemplateConfig;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestRestTemplateConfig.class)
@Import(RestTemplateConfig.class)
@Import(RestTemplateConfig.class)
@Import(RestTemplateConfig.class)
@Import(RestTemplateConfig.class)
@Import(RestTemplateConfig.class)
public class TestControllerIT {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @LocalServerPort
    private int port;
    
    @Test
    public void testEndpoint() {
        String result = restTemplate.getForObject("http://localhost:" + port + "/api/test", String.class);
        // Test should now pass with correct imports
    }
}
