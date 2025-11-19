package com.project.asset.repository;

import com.project.asset.domain.entity.Purchase;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByPurchaseNo(String purchaseNo);
}


