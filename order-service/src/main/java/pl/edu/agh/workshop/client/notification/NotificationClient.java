package pl.edu.agh.workshop.client.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pl.edu.agh.workshop.exception.NotificationServiceUnavailableException;

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
        //todo: Send POST request to notification-service
        throw new UnsupportedOperationException("TODO: implement notification call");
    }
}
