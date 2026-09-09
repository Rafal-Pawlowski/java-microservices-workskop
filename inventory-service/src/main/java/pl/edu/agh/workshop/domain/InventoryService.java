package pl.edu.agh.workshop.domain;

import org.springframework.stereotype.Service;
import pl.edu.agh.workshop.api.InventoryResponse;

import java.util.Map;
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
}
