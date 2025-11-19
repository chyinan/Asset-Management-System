package com.project.asset.repository;

import com.project.asset.domain.entity.Asset;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    @EntityGraph(attributePaths = {"assetType"})
    Optional<Asset> findByAssetNo(String assetNo);

    boolean existsByAssetType_Id(Long assetTypeId);
}


