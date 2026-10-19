package pl.edu.agh.workshop.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotBlank String productId,
        @Positive int quantity,
        @NotBlank @Email String customerEmail
) {
}
