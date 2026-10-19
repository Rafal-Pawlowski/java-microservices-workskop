package pl.edu.agh.workshop.notification.domain;

/*
Wynik domenowy, nie jest odpowiedzią HTTP
*/

import java.time.Instant;
import java.util.UUID;

public record NotificationResult(
        UUID notificationId,
        UUID orderId,
        String recipient,
        Instant sentAt,
        NotificationStatus status
) {
}
