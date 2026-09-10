package pl.edu.agh.workshop.domain;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String productId, int requestedQuantity, int availableQuantity) {
        super("Not enough stock for product %s. Requested: %d, available: %d"
                .formatted(productId, requestedQuantity, availableQuantity));
    }
}
