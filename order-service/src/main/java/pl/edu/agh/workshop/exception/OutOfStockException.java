package pl.edu.agh.workshop.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String productId, int quantity   ) {
        super("Cannot reserve " + quantity + " units of product " + productId);
    }
}
