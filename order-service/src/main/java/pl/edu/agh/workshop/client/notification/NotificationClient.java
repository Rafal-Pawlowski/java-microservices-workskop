package pl.edu.agh.workshop.client.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class NotificationClient {

    private final RestClient restClient;

    public NotificationClient(@Qualifier("notificationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public NotificationClientResponse send(UUID orderId,
                                           String customerEmail,
                                           String message) {
        SendNotificationRequest request = new SendNotificationRequest(
                orderId,
                customerEmail,
                message
        );
        return restClient.post()
                .uri("/api/notifications")
                .body(request)
                .retrieve()
                .body(NotificationClientResponse.class);
    }
}
