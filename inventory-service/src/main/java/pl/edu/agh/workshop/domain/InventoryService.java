package pl.edu.agh.workshop.domain;

import org.springframework.stereotype.Service;
import pl.edu.agh.workshop.api.InventoryResponse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
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

        return new InventoryReservation(
                UUID.randomUUID(),
                productId,
                quantity,
                remainingQuantity,
                ReservationStatus.CONFIRMED
        );
    }
}
