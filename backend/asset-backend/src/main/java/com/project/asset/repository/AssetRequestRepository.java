package com.project.asset.repository;

import com.project.asset.domain.entity.AssetRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRequestRepository extends JpaRepository<AssetRequest, Long> {
    Optional<AssetRequest> findByRequestNo(String requestNo);
}


