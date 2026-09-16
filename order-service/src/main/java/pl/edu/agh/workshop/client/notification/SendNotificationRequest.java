package pl.edu.agh.workshop.client.notification;

import java.util.UUID;

public record SendNotificationRequest(
        UUID orderId,
        String customerEmail,
        String message
) {
}
