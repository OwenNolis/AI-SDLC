package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BrokenTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testWillFail() {
        // Test should now pass with correct imports
    }
}
