package pl.edu.agh.workshop.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.edu.agh.workshop.exception.InventoryServiceUnavailableException;
import pl.edu.agh.workshop.exception.NotificationServiceUnavailableException;
import pl.edu.agh.workshop.exception.OutOfStockException;
import pl.edu.agh.workshop.exception.ProductNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleProductNotFound(ProductNotFoundException exception) {
        return createResponse(HttpStatus.NOT_FOUND,
                "Product not found",
                exception.getMessage());
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ProblemDetail> handleOutOfStock(OutOfStockException exception) {
        return createResponse(HttpStatus.CONFLICT, "Insufficient stock", exception.getMessage());
    }

    @ExceptionHandler(InventoryServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleInventoryUnavailable(InventoryServiceUnavailableException exception) {
        return createResponse(HttpStatus.SERVICE_UNAVAILABLE, "Inventory service unavailable", exception.getMessage());
    }

    @ExceptionHandler(NotificationServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleNotificationUnavailable( NotificationServiceUnavailableException exception) {
        return createResponse(HttpStatus.SERVICE_UNAVAILABLE, "Notification service unavailable", exception.getMessage());
    }

    private ResponseEntity<ProblemDetail> createResponse(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);

        return ResponseEntity
                .status(status)
                .body(problem);
    }
}
