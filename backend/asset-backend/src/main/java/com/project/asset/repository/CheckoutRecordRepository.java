package com.project.asset.repository;

import com.project.asset.domain.entity.CheckoutRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutRecordRepository extends JpaRepository<CheckoutRecord, Long> {
    List<CheckoutRecord> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId);
}


