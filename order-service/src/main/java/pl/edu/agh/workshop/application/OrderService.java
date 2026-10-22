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
        /*
        * todo:
        *  1. Generate orderId.
        *  2. Reserve the product using InventoryClient
        *  3. Send a notification using NotificationClient
        *  4. Return OrderResult
        *  */
        throw new UnsupportedOperationException("TODO: implement order creation flow");
    }
}
