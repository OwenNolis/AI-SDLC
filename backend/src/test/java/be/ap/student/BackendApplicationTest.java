package be.ap.student;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@SpringBootTest
public class BackendApplicationTest {

    @Test
    void mainMethod_runsApplication() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            BackendApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(eq(BackendApplication.class), any(String[].class)), times(1));
        }
    }
}
