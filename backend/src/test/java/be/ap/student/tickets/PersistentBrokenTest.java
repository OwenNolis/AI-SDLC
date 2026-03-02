package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate; // OLD IMPORT - WILL FAIL IN SPRING BOOT 4+
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersistentBrokenTest {
    @Autowired
    private TestRestTemplate restTemplate; // WILL CAUSE COMPILATION ERROR
    
    @Test
    public void testWillFailDueToBadImport() {
        // This test will fail due to Spring Boot 4.x import changes
        // Should be: org.springframework.boot.resttestclient.TestRestTemplate
    }
}