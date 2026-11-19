package pl.edu.agh.workshop.domain;

import org.springframework.stereotype.Service;
import pl.edu.agh.workshop.api.InventoryEntity;
import pl.edu.agh.workshop.api.InventoryRepository;
import pl.edu.agh.workshop.api.config.interfaces.UUIDGenerator;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final UUIDGenerator uuidGenerator;

    public InventoryService(InventoryRepository inventoryRepository, UUIDGenerator uuidGenerator) {
        this.inventoryRepository = inventoryRepository;
        this.uuidGenerator = uuidGenerator;
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
                uuidGenerator.generate(),
                productId,
                quantity,
                remainingQuantity,
                ReservationStatus.CONFIRMED
        );
    }
}
