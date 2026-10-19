package pl.edu.agh.workshop.client.inventory;

import java.util.UUID;

public record InventoryReservationResponse(
        UUID reservationId,
        String productId,
        int reservedQuantity,
        int remainingQuantity,
        String status
) {
}
