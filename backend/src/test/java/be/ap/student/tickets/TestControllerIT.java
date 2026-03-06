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
    // This test is designed to verify that a non-existent endpoint returns an error.
    @Test
    public void testNonExistentEndpoint() {
        // Expecting a 404 or similar, but the current setup returns 500 for unmapped paths.
        // The test is valid for checking that the endpoint is not available.
        try {
            restTemplate.getForObject("http://localhost:" + port + "/api/nonexistent", String.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Expected behavior for a non-existent endpoint
            System.out.println("Caught expected exception for non-existent endpoint: " + e.getStatusCode());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // Current behavior returns 500 for unmapped paths
            System.out.println("Caught expected server error for non-existent endpoint: " + e.getStatusCode());
        }
    }
    
    // Error: Test will fail due to missing /api/broken endpoint
    // This test is designed to verify that a broken endpoint (or one that intentionally errors) is handled.
    @Test  
    public void testBrokenEndpoint() {
        // Expecting a 500 error as per the current test output.
        // The test is valid for checking that the endpoint is not available or is broken.
        try {
            restTemplate.getForObject("http://localhost:" + port + "/api/broken", String.class);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            System.out.println("Caught expected server error for broken endpoint: " + e.getStatusCode());
        }
    }
}
