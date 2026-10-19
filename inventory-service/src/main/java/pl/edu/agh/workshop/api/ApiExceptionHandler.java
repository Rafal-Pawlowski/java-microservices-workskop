package pl.edu.agh.workshop.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.edu.agh.workshop.domain.OutOfStockException;
import pl.edu.agh.workshop.domain.ProductNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Product Not Found");
        problem.setProperty("errorCode", "PRODUCT_NOT_FOUND");
        return problem;
    }

    @ExceptionHandler(OutOfStockException.class)
    public ProblemDetail handleOutOfStock(OutOfStockException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());

        problem.setTitle("Insufficient inventory");
        problem.setProperty("errorCode", "OUT_OF_STOCK");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());

        problem.setTitle("Invalid request");
        problem.setProperty("errorCode", "INVALID_REQUEST");
        return problem;
    }
}
