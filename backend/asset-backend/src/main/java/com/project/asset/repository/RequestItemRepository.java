package com.project.asset.repository;

import com.project.asset.domain.entity.RequestItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
    List<RequestItem> findByRequestId(Long requestId);
}


