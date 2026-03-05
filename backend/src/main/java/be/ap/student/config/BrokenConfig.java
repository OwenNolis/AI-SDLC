package be.ap.student.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.ArrayList;

@Configuration
public class BrokenConfig {

    @Bean
    public List<String> configItems() {
        return new ArrayList<>();
    }

    @Bean
    public String configValue() {
        return "default-config-value";
    }
}
