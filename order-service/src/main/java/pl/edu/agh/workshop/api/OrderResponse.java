package pl.edu.agh.workshop.api;

import pl.edu.agh.workshop.domain.OrderResult;

import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID reservationId,
        String productId,
        int quantity,
        String notificationStatus
) {

    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(
                result.orderId(),
                result.reservationId(),
                result.productId(),
                result.quantity(),
                result.notificationStatus()
        );
    }
}
