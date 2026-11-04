package pl.edu.agh.workshop.client.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@Slf4j
public class NotificationClient {

    //todo to pod spodem również zaimplementować podczas warsztatów i wskazać to jako zadanie dla studentów
    private final RestClient restClient;

    public NotificationClient(@Qualifier("notificationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public NotificationClientResponse send(UUID orderId, String customerEmail, String message) {
        log.info("[ORDER -> NOTIFICATION] Sending notification request: orderId={}", orderId);

        /**
         * todo: Send POST request to notification-service
         * TODO 4: utwórz SendNotificationRequest
         * TODO 5: wykonaj POST do /api/notifications
         * TODO 6: odczytaj NotificationclientResponse
         * */
        throw new UnsupportedOperationException("TODO: implement notification call");
    }
}
