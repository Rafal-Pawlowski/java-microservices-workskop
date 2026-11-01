import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@TestConfiguration
public class TestConfig {

    @Bean
    private UUID randomUUID() {
        return UUID.fromString("test-UUID");
    }
}
