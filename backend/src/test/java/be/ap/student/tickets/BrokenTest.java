package be.ap.student.tickets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate; // OLD IMPORT - WILL FAIL
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BrokenTest {
    @Autowired
    private TestRestTemplate restTemplate; // WILL CAUSE COMPILATION ERROR
    
    @Test
    public void testWillFail() {
        // This test will fail due to import error
    }
}
