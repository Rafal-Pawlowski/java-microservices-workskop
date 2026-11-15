package pl.edu.agh.workshop.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.edu.agh.workshop.api.config.interfaces.UUIDGenerator;

import java.util.UUID;

@Configuration
@Profile("!test")
public class Config {

    @Bean
    public UUIDGenerator uuidGenerator() {
        return UUID::randomUUID;
    }
}
