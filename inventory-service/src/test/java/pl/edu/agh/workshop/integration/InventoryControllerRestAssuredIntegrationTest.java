package pl.edu.agh.workshop.integration;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import pl.edu.agh.workshop.TestcontainersConfiguration;
import pl.edu.agh.workshop.api.InventoryResponse;
import pl.edu.agh.workshop.api.ReservationRequest;
import pl.edu.agh.workshop.api.ReservationResponse;
import pl.edu.agh.workshop.domain.InventoryEntity;
import pl.edu.agh.workshop.domain.InventorySnapshot;
import pl.edu.agh.workshop.domain.ReservationStatus;

import java.net.URI;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(
        scripts = "/db/test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class InventoryControllerRestAssuredIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldGetInventory() {
        //given
        var inventorySnapshot = new InventorySnapshot("test-product_id1", 4);

        //when
        var responseEntity = given()
                    .baseUri("http://localhost")
                    .port(serverPort)
                .when()
                    .get("/api/inventory/test-product_id1")
                .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(InventoryResponse.class);

        //then
        assertEquals(InventoryResponse.from(inventorySnapshot), responseEntity);
    }

    @Test
    void shouldNotGetInventory() {
        //given
        var productId = "not-exist-product-id";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Product not found: " + productId);
        problem.setTitle("Product Not Found");
        problem.setInstance(URI.create("/api/inventory/" + productId));

        //when
        var responseEntity = given()
                    .baseUri("http://localhost")
                    .port(serverPort)
                .when()
                .   get("/api/inventory/" + productId)
                .then()
                    .statusCode(404)
                    .extract()
                    .body()
                    .as(ProblemDetail.class);

        //then
        assertEquals(problem, responseEntity);
    }

    @Test
    void shouldCreateInventoryReservation() {
        //given
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
        var responseEntity = given()
                    .baseUri("http://localhost")
                    .port(serverPort)
                    .contentType(ContentType.JSON)
                .when()
                    .body(body)
                    .post("/api/inventory/reservations")
                .then()
                    .statusCode(201)
                    .extract()
                    .body()
                    .as(ReservationResponse.class);

        //then
        assertEquals(expectedBody, responseEntity);

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

    //TODO

    @Test
    void shouldNotCreateInventoryReservationByQuantityIsLowerThan0() {}

    @Test
    void shouldNotCreateInventoryReservationByProductNotFoundException() {}

    @Test
    void shouldNotCreateInventoryReservationByOutOfStockException() {}
}
