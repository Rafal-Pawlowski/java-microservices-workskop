package pl.edu.agh.workshop.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.workshop.application.OrderService;
import pl.edu.agh.workshop.domain.OrderResult;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResult result = orderService.createOrder(
                request.productId(),
                request.quantity(),
                request.customerEmail()
        );
        return OrderResponse.from(result);
    }
}
