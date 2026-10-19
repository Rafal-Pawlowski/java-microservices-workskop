package pl.edu.agh.workshop.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product " + productId + " was not found");
    }
}
