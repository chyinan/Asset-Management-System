package com.project.asset.repository;

import com.project.asset.domain.entity.Approval;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByEntityTypeAndEntityIdOrderByCreatedAtAsc(String entityType, Long entityId);
}


