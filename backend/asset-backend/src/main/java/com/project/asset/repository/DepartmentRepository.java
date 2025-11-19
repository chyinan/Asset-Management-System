package com.project.asset.repository;

import com.project.asset.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByParent_Id(Long parentId);
}


