package pl.edu.agh.workshop.client.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class InventoryClient {

    //todo to poniżej również można jako livecoding, żeby pokazać jak przygotować klasę klienta
    public InventoryClient(@Qualifier("inventoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    private final RestClient restClient;

    public InventoryReservationResponse reserve(String productId, int quantity) {
        log.info("[ORDER -> INVENTORY] Sending reservation request: productId={}, quantity={}", productId, quantity);

        /** todo send POST request to inventory-service
         * TODO 1: utwórz InventoryReservationRequest
         * TODO 2: wykonaj POST do /api/inventory/reservations
         * TODO 3: odczytaj InventoryReservationResponse
         * */
        throw new UnsupportedOperationException("TODO: implement inventory reservation call");
    }
}
