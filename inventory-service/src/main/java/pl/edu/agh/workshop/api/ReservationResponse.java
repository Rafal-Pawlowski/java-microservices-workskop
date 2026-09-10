package pl.edu.agh.workshop.api;

import pl.edu.agh.workshop.domain.InventoryReservation;

import java.util.UUID;

public record ReservationResponse(
        UUID reservationId,
        String productId,
        int reservedQuantity,
        int remainingQuantity,
        String status
) {

    static ReservationResponse from(InventoryReservation reservation){
        return new ReservationResponse(
                reservation.reservationId(),
                reservation.productId(),
                reservation.reservedQuantity(),
                reservation.remainingQuantity(),
                reservation.status().name()
        );
    }
}
