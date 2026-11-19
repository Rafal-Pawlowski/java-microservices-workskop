package pl.edu.agh.workshop;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import pl.edu.agh.workshop.api.config.interfaces.UUIDGenerator;

import java.util.UUID;

@TestConfiguration
public class TestConfig {

    @Bean
    @Profile("test")
    public UUIDGenerator uuidGenerator() {
        UUID fixedUuid = UUID.fromString(
                "123e4567-e89b-12d3-a456-426614174001"
        );

        return () -> fixedUuid;
    }
}
