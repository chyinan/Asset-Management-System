package com.project.asset.repository;

import com.project.asset.domain.entity.Asset;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByAssetNo(String assetNo);
}


