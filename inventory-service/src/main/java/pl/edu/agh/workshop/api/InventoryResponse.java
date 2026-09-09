package pl.edu.agh.workshop.api;

import pl.edu.agh.workshop.domain.InventorySnapshot;

/*
Record
* automatic constructor
* get methods
* equals, hashcode, toString
* */
public record InventoryResponse(String productId,
                                int availableQuantity,
                                boolean available) {

    static InventoryResponse from(InventorySnapshot snapshot) {
        return new InventoryResponse(
                snapshot.productId(),
                snapshot.availableQuantity(),
                snapshot.available()
        );
    }
}
