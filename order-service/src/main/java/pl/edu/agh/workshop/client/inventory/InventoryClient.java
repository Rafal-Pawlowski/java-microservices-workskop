package pl.edu.agh.workshop.client.inventory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Qualifier("inventoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public InventoryReservationResponse reserve(String productId, int quantity) {
        InventoryReservationRequest request = new InventoryReservationRequest(productId, quantity);
        return restClient.post()
                .uri("/api/inventory/reservations")
                .body(request)
                .retrieve()
                .body(InventoryReservationResponse.class);
    }

}
