package pl.edu.agh.workshop.domain;

import java.util.UUID;

public record OrderResult(
        UUID orderId,
        UUID reservationId,
        String productId,
        int quantity,
        String notificationStatus
) {
}
