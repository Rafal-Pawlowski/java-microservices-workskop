package pl.edu.agh.workshop.client.inventory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pl.edu.agh.workshop.exception.InventoryServiceUnavailableException;
import pl.edu.agh.workshop.exception.OutOfStockException;
import pl.edu.agh.workshop.exception.ProductNotFoundException;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Qualifier("inventoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public InventoryReservationResponse reserve(String productId, int quantity) {
        try {
            InventoryReservationRequest request = new InventoryReservationRequest(productId, quantity);
            return restClient.post()
                    .uri("/api/inventory/reservations")
                    .body(request)
                    .retrieve()
                    .body(InventoryReservationResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        } catch (HttpClientErrorException.Conflict e) {
            throw new OutOfStockException(productId, quantity);
        } catch (RestClientException e) {
            throw new InventoryServiceUnavailableException(e);
        }
    }

}
