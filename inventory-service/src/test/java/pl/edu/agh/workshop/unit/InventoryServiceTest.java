package pl.edu.agh.workshop.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.workshop.api.InventoryEntity;
import pl.edu.agh.workshop.api.InventoryRepository;
import pl.edu.agh.workshop.domain.*;
import pl.edu.agh.workshop.api.config.interfaces.UUIDGenerator;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository repository;

    @Mock
    private UUIDGenerator uuidGenerator;

    @InjectMocks
    private InventoryService service;

    @Test
    void getInventorySnapshotByProductId() {
        //given
        var uuid = UUID.randomUUID();

        var entity = new InventoryEntity();
        entity.setUuid(uuid);
        entity.setProductId("test-product_id1");
        entity.setQuantity(10);

        when(repository.findInventoryEntityByProductId("test-product_id1")).thenReturn(entity);

        //when
        var inventorySnapshot = service.getInventory("test-product_id1");
        //then
        assertEquals(inventorySnapshot, new InventorySnapshot(entity.getProductId(), entity.getQuantity()));
    }

    @Test
    void getNoSuchExceptionWhenTryGetInventorySnapshotByNoExistingProductId() {
        //given
        when(repository.findInventoryEntityByProductId("no-existing-id")).thenReturn(null);

        //when + then
        assertThrows(
                ProductNotFoundException.class,
                () -> service.getInventory("no-existing-id")
        );
    }

    @Test
    void getInventoryReservation() {

        UUID fixedUuidForEntity =
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID fixedUuidForReservation =
                UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

        try (MockedStatic<UUID> mockedUUID =
                     mockStatic(UUID.class)) {

            //given
            mockedUUID.when(UUID::randomUUID)
                    .thenReturn(fixedUuidForReservation);

            String productId = "test-product_id1";

            var entity = new InventoryEntity();
            entity.setUuid(fixedUuidForEntity);
            entity.setProductId(productId);
            entity.setQuantity(20);

            var inventoryReservationCheck = new InventoryReservation(
                    fixedUuidForReservation,
                    productId,
                    10,
                    10,
                    ReservationStatus.CONFIRMED
            );

            when(repository.findInventoryEntityByProductId(productId)).thenReturn(entity);
            when(uuidGenerator.generate()).thenReturn(fixedUuidForReservation);

            //when
            var inventoryReservation = service.reserve(productId, 10);
            //then
            verify(repository).save(argThat(inventoryEntity ->
                    inventoryEntity.getUuid().equals(fixedUuidForEntity)
                            && inventoryEntity.getProductId().equals(productId)
                            && inventoryEntity.getQuantity().equals(10)
            ));
            assertEquals(inventoryReservation, inventoryReservationCheck);
        }
    }

    //TODO: Implement test methods to check all possible exceptions

    @Test
    void noGetInventoryReservationByQuantityIsLowerThan0() {}

    @Test
    void noGetInventoryReservationByProductNotFoundException() {}

    @Test
    void noGetInventoryReservationByOutOfStockException() {}
}