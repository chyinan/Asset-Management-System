package com.project.asset.repository;

import com.project.asset.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByDepartment_Id(Long departmentId);
}

