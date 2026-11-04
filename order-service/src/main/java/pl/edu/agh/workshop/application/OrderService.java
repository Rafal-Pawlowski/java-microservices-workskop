package pl.edu.agh.workshop.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.workshop.client.inventory.InventoryClient;
import pl.edu.agh.workshop.client.inventory.InventoryReservationResponse;
import pl.edu.agh.workshop.client.notification.NotificationClient;
import pl.edu.agh.workshop.client.notification.NotificationClientResponse;
import pl.edu.agh.workshop.domain.OrderResult;

import java.util.UUID;

@Service
@Slf4j
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
        log.info("[ORDER] Starting order creation: productId={}, quantity={}", productId, quantity);

        UUID orderId = UUID.randomUUID();

        log.info(
                "[ORDER] Creating order. orderId={}, productId={}, quantity={}",
                orderId,
                productId,
                quantity
        );

        InventoryReservationResponse reservation =
                inventoryClient.reserve(productId, quantity);

        log.info(
                "[ORDER] Inventory reserved. orderId={}, reservationId={}",
                orderId,
                reservation.reservationId()
        );

        NotificationClientResponse notification =
                notificationClient.send(
                        orderId,
                        customerEmail,
                        "Order " + orderId + " has been created"
                );

        log.info(
                "[ORDER] Notification completed. orderId={}, status={}",
                orderId,
                notification.status()
        );

        return new OrderResult(
                orderId,
                reservation.reservationId(),
                productId,
                quantity,
                notification.status()
        );
//        /*
//         * TODO 7: wygeneruj orderId
//         * TODO 8: wywołaj InventoryClient
//         * TODO 9: wywołaj NotificationClient
//         * TODO 10: utwórz i zwróć OrderResult
//         *  */
//        throw new UnsupportedOperationException("TODO: implement order creation flow");
    }
}
