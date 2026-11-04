package pl.edu.agh.workshop.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.workshop.domain.InventoryReservation;
import pl.edu.agh.workshop.domain.InventoryService;
import pl.edu.agh.workshop.domain.InventorySnapshot;

@RestController
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public InventoryResponse getInventory(@PathVariable String productId) {

        InventorySnapshot snapshot = inventoryService.getInventory(productId);
        return InventoryResponse.from(snapshot);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@Valid @RequestBody ReservationRequest request) {
        log.info("[INVENTORY] HTTP reservation request received: productId={}, quantity={}", request.productId(),  request.quantity());

        InventoryReservation reservation = inventoryService.reserve(
                request.productId(),
                request.quantity()
                );
        return ReservationResponse.from(reservation);
    }
}
