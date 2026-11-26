package pl.edu.agh.workshop.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.workshop.client.inventory.InventoryClient;
import pl.edu.agh.workshop.client.notification.NotificationClient;
import pl.edu.agh.workshop.domain.OrderResult;

@Service
@Slf4j
public class OrderService {

    private final InventoryClient inventoryClient;
    private final NotificationClient notificationClient;

    public OrderService(InventoryClient inventoryClient, NotificationClient notificationClient) {
        this.inventoryClient = inventoryClient;
        this.notificationClient = notificationClient;
    }

    public OrderResult createOrder(String productId, int quantity, String customerEmail) {
        throw new UnsupportedOperationException("TODO: implement order creation flow");
    }
}
