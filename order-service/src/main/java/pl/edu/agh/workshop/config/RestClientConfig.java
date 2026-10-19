package pl.edu.agh.workshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient inventoryRestClient(
            RestClient.Builder builder,
            @Value("${services.inventory.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    RestClient notificationRestClient(
            RestClient.Builder builder,
            @Value("${services.notification.base-url}") String baseUrl){
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
