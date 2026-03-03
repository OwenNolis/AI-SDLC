package be.ap.student.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRestTemplateConfig {
    
    @Bean
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate();
    }
}
