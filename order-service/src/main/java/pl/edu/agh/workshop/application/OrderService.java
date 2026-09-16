package pl.edu.agh.workshop.application;

import org.springframework.stereotype.Service;
import pl.edu.agh.workshop.client.inventory.InventoryClient;
import pl.edu.agh.workshop.client.inventory.InventoryReservationResponse;
import pl.edu.agh.workshop.client.notification.NotificationClient;
import pl.edu.agh.workshop.client.notification.NotificationClientResponse;
import pl.edu.agh.workshop.domain.OrderResult;

import java.util.Objects;
import java.util.UUID;

@Service
public class OrderService {

    private final InventoryClient inventoryClient;
    private final NotificationClient notificationClient;

    public OrderService(InventoryClient inventoryClient, NotificationClient notificationClient) {
        this.inventoryClient = inventoryClient;
        this.notificationClient = notificationClient;
    }

    public OrderResult createOrder(String productId,
                                   int quantity,
                                   String customerEmail) {
        UUID orderId = UUID.randomUUID();
        InventoryReservationResponse reservation =
                Objects.requireNonNull(
                        inventoryClient.reserve(productId, quantity),
                        "Inventory service returned an empty response"
                );
        NotificationClientResponse notificationn =
                Objects.requireNonNull(
                        notificationClient.send(
                                orderId,
                                customerEmail,
                                "Your order " + orderId + " has been created."
                        ),
                        "Notification service returned an empty response"
                );
        return new OrderResult(orderId, reservation.reservationId(), productId, quantity, notificationn.status());
    }
}
