package be.ap.student.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.web.server.LocalServerPort;

@TestConfiguration
public class TestRestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(@LocalServerPort int port) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new org.springframework.web.util.DefaultUriBuilderFactory("http://localhost:" + port));
        return restTemplate;
    }
}
