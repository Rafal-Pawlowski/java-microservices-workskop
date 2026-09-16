package pl.edu.agh.workshop.notification.api;

import pl.edu.agh.workshop.notification.domain.NotificationResult;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        UUID orderId,
        String recipient,
        Instant sentAt,
        String status
) {

    static NotificationResponse from(NotificationResult result) {
        return new NotificationResponse(
                result.notificationId(),
                result.orderId(),
                result.recipient(),
                result.sentAt(),
                result.status().name()
        );
    }
}
