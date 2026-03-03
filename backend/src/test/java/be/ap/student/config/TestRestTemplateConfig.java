package be.ap.student.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.web.server.LocalServerPort;

@TestConfiguration
public class TestRestTemplateConfig {
    
    @Bean
    public TestRestTemplate testRestTemplate(@LocalServerPort int port) {
        TestRestTemplate template = new TestRestTemplate();
        template.setRootUri("http://localhost:" + port);
        return template;
    }
}
