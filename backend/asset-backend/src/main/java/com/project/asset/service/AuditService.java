package com.project.asset.service;

import com.project.asset.domain.entity.AuditLog;
import com.project.asset.domain.entity.User;
import com.project.asset.repository.AuditLogRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final EntityManager entityManager;

    public void record(Long userId, String action, String entity, Long entityId, String detail) {
        AuditLog log = new AuditLog();
        if (userId != null) {
            User reference = entityManager.getReference(User.class, userId);
            log.setUser(reference);
        }
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}

