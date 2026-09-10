package pl.edu.agh.workshop.domain;

import java.util.UUID;

public record InventoryReservation(UUID reservationId,
                                   String productId,
                                   int reservedQuantity,
                                   int remainingQuantity,
                                   ReservationStatus status) {
}
