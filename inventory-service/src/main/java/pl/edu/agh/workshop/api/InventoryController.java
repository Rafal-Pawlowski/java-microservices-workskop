package pl.edu.agh.workshop.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.workshop.domain.InventoryService;
import pl.edu.agh.workshop.domain.InventorySnapshot;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    //dependency injection
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    @GetMapping("/{productId}")
    public InventoryResponse getInventory(@PathVariable String productId){

        InventorySnapshot snapshot = inventoryService. getInventory(productId);
        return InventoryResponse.from(snapshot);
    }
}
