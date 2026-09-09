package pl.edu.agh.workshop.domain;

public record InventorySnapshot(String productId, int availableQuantity) {

    public boolean available() {
        return availableQuantity > 0;
    }
}
