package pl.edu.agh.workshop.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InventoryService {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>(
            Map.of("LAPTOP-001", 10,
                    "PHONE-001", 5,
                    "HEADPHONES-001", 0)
    );

    public InventorySnapshot getInventory(String productId) {
        int availableQuantity = stock.getOrDefault(productId, 0);
        return new InventorySnapshot(
                productId,
                availableQuantity
        );
    }

    public synchronized InventoryReservation reserve(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        Integer availableQuantity = stock.get(productId);

        if (availableQuantity == null) {
            throw new ProductNotFoundException(productId);
        }

        if (availableQuantity < quantity) {
            throw new OutOfStockException(productId, quantity, availableQuantity);
        }

        int remainingQuantity = availableQuantity - quantity;

        stock.put(productId, remainingQuantity);

        UUID reservationId = UUID.randomUUID();
        log.info("[INVENTORY] Reservation created:\n " +
                        "reservationId={}, productId={}, \n " +
                        "reservedQuantity={}, remainingQuantity={}",
                reservationId,
                productId,
                quantity,
                remainingQuantity
        );

        return new InventoryReservation(
                UUID.randomUUID(),
                productId,
                quantity,
                remainingQuantity,
                ReservationStatus.CONFIRMED
        );
    }
}
