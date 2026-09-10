package pl.edu.agh.workshop.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

                                //@Valid in controller turn on annotations
public record ReservationRequest(@NotBlank(message = "Product id must not be blank") String productId,
                                 @Positive(message = "Quantity must be greater than zero") int quantity) {

}
