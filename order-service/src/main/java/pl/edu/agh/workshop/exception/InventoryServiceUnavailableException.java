package pl.edu.agh.workshop.exception;

public class InventoryServiceUnavailableException extends RuntimeException {
    public InventoryServiceUnavailableException(Throwable cause) {
        super("Inventory service is unavailable", cause);
    }
}

