package pl.edu.agh.workshop.notification.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/*
Kontrakt wejściowy HTTP
*/

public record NotificationRequest(
        @NotNull(message="Order id must not be null")
        UUID orderId,

        @NotBlank(message = "Customer email must not be blank")
        @Email(message = "Customer email must be valid")
        String customerEmail,

        @NotBlank(message = "Message must not be blank")
        String message
) {
}
