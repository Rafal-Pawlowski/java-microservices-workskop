package pl.edu.agh.workshop.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;

@Controller
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    InventoryEntity findInventoryEntityByProductId(String productId);
}
