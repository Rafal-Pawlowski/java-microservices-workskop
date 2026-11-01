package pl.edu.agh.workshop.domain;

import org.springframework.stereotype.Service;
import pl.edu.agh.workshop.api.InventoryResponse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventorySnapshot getInventory(String productId) {
        InventoryEntity inventoryEntity = inventoryRepository.findInventoryEntityByProductId(productId);

        if (inventoryEntity == null) {
            throw new ProductNotFoundException(productId);
        }

        int availableQuantity= inventoryEntity.getQuantity();

        return new InventorySnapshot(
                productId,
                availableQuantity
        );
    }

    public synchronized InventoryReservation reserve(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        InventoryEntity entity = inventoryRepository.findInventoryEntityByProductId(productId);

        if (entity == null) {
            throw new ProductNotFoundException(productId);
        }

        if (entity.getQuantity() < quantity) {
            throw new OutOfStockException(productId, quantity, entity.getQuantity());
        }

        int remainingQuantity = entity.getQuantity() - quantity;
        entity.setQuantity(remainingQuantity);

        inventoryRepository.save(entity);

        return new InventoryReservation(
                UUID.randomUUID(),
                productId,
                quantity,
                remainingQuantity,
                ReservationStatus.CONFIRMED
        );
    }
}
