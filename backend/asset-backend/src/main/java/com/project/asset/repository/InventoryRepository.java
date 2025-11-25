package com.project.asset.repository;

import com.project.asset.domain.entity.Inventory;
import com.project.asset.domain.enums.InventoryStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySerialNo(String serialNo);

    List<Inventory> findByStatus(InventoryStatus status);

    List<Inventory> findByStatusAndExpectedReturnAtBefore(
            InventoryStatus status, LocalDateTime expectedReturnAt);
}


