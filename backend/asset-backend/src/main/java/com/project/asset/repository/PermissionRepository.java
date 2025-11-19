package com.project.asset.repository;

import com.project.asset.domain.entity.Permission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findByCodeIn(Collection<String> codes);
}


