package com.project.asset.repository;

import com.project.asset.domain.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {
    boolean existsByCodeAndIdNot(String code, Long id);
    boolean existsByCode(String code);
}


