package pl.edu.agh.workshop.integration;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import pl.edu.agh.workshop.TestcontainersConfiguration;
import pl.edu.agh.workshop.api.InventoryResponse;
import pl.edu.agh.workshop.api.ReservationRequest;
import pl.edu.agh.workshop.api.ReservationResponse;
import pl.edu.agh.workshop.api.InventoryEntity;
import pl.edu.agh.workshop.domain.InventorySnapshot;
import pl.edu.agh.workshop.domain.ReservationStatus;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(
        scripts = "/db/test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class InventoryControllerRestTemplateIntegrationTest extends TestcontainersConfiguration {

    private final JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
    private final RestTemplate restTemplate = new RestTemplate(requestFactory);

    private final String baseUri = "http://localhost:";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup(){
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(@NonNull HttpStatusCode statusCode) {
                return false;
            }
        });
    }

    @Test
    void shouldGetInventory() {
        //given
        var endpoint = "/api/inventory/test-product_id1";
        var uri = baseUri + serverPort + endpoint;
        var inventorySnapshot = new InventorySnapshot("test-product_id1", 4);

        //when
        var responseBody = restTemplate.exchange(
                URI.create(uri),
                HttpMethod.GET,
                null,
                InventoryResponse.class
        );

        //then
        assertEquals(InventoryResponse.from(inventorySnapshot), responseBody.getBody());
    }

    @Test
    void shouldNotGetInventory() {
        //given
        var productId = "not-exist-product-id";
        var endpoint = "/api/inventory/" + productId;
        var uri = baseUri + serverPort + endpoint;

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Product not found: " + productId);
        problem.setTitle("Product Not Found");
        problem.setInstance(URI.create("/api/inventory/" + productId));
        problem.setProperties(Map.of("errorCode", "PRODUCT_NOT_FOUND"));

        //when
        var responseBody = restTemplate.exchange(
                URI.create(uri),
                HttpMethod.GET,
                null,
                ProblemDetail.class
        );

        //then
        assertEquals(HttpStatusCode.valueOf(404), responseBody.getStatusCode());
        assertEquals(problem, responseBody.getBody());
    }

    @Test
    void shouldCreateInventoryReservation() {
        //given
        var endpoint = "/api/inventory/reservations";
        var uri = baseUri + serverPort + endpoint;

        String productId = "test-product_id1";
        UUID reservationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

        var expectedResultFromDB = new InventoryEntity();
        expectedResultFromDB.setUuid(UUID.fromString("941c5afe-4bc4-4ed9-bb11-6c1e9c45f2b3"));
        expectedResultFromDB.setProductId(productId);
        expectedResultFromDB.setQuantity(2);

        var body = new ReservationRequest(productId, 2);

        var expectedBody = new ReservationResponse(
                reservationId,
                productId,
                2,
                2,
                ReservationStatus.CONFIRMED.name()
        );
        //when
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(body, headers);

        var responseBody = restTemplate.exchange(
                URI.create(uri),
                HttpMethod.POST,
                request,
                ReservationResponse.class
        );

        //then
        assertEquals(expectedBody, responseBody.getBody());

        var result = jdbcTemplate.queryForObject(
                "SELECT * FROM inventory WHERE product_id = ?",
                new BeanPropertyRowMapper<>(InventoryEntity.class),
                productId
        );
        assertAll(
                () -> assertEquals(expectedResultFromDB.getUuid(), result.getUuid()),
                () -> assertEquals(expectedResultFromDB.getProductId(), result.getProductId()),
                () -> assertEquals(expectedResultFromDB.getQuantity(), result.getQuantity())
        );
    }

    @Test
    void shouldNotCreateInventoryReservationByQuantityIsLowerThan0() {
        //given
        var endpoint = "/api/inventory/reservations";
        var uri = baseUri + serverPort + endpoint;

        String productId = "test-product_id1";

        var body = new ReservationRequest(productId, 0);

        //when
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(body, headers);

        var responseBody = restTemplate.exchange(
                URI.create(uri),
                HttpMethod.POST,
                request,
                ProblemDetail.class
        );

        //then
        assertEquals(HttpStatusCode.valueOf(400), responseBody.getStatusCode());
    }

    @Test
    void shouldNotCreateInventoryReservationByProductNotFoundException() {
        //given
        var endpoint = "/api/inventory/reservations";
        var uri = baseUri + serverPort + endpoint;

        String productId = "not-existing-id";
        var body = new ReservationRequest(productId, 2);

        //when
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(body, headers);

        var responseBody = restTemplate.exchange(
                URI.create(uri),
                HttpMethod.POST,
                request,
                ProblemDetail.class
        );

        //then
        assertEquals(HttpStatusCode.valueOf(404), responseBody.getStatusCode());
        assert responseBody.getBody() != null;
        assertEquals("Product not found: %s".formatted(productId), responseBody.getBody().getDetail());
    }

    @Test
    void shouldNotCreateInventoryReservationByOutOfStockException() {
        //given
        var endpoint = "/api/inventory/reservations";
        var uri = baseUri + serverPort + endpoint;

        String productId = "test-product_id1";
        var body = new ReservationRequest(productId, 200);

        //when
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(body, headers);

        var responseBody = restTemplate.exchange(
                URI.create(uri),
                HttpMethod.POST,
                request,
                ProblemDetail.class
        );

        //then
        assertEquals(HttpStatusCode.valueOf(409), responseBody.getStatusCode());
        assert responseBody.getBody() != null;
        assertEquals("Not enough stock for product %s. Requested: %d, available: %d".formatted(productId, 200, 4), responseBody.getBody().getDetail());
    }
}
