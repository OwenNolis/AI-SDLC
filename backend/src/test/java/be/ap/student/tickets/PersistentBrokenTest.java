package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import be.ap.student.config.TestRestTemplateConfig;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestRestTemplateConfig.class)
public class PersistentBrokenTest {
    @Autowired
    private RestTemplate restTemplate;
    
    @Test
    public void testWillFailDueToBadImport() {
        // This test should now work with RestTemplate properly injected
        System.out.println("RestTemplate injected: " + (restTemplate != null));
    }
}
