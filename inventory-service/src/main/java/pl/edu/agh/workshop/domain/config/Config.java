package pl.edu.agh.workshop.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.edu.agh.workshop.domain.config.interfaces.UUIDGenerator;

import java.util.UUID;

@Configuration
@Profile("!test")
public class Config {

    @Bean
    public UUIDGenerator uuidGenerator() {
        return UUID::randomUUID;
    }
}
