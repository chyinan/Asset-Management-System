package com.project.asset.service;

import com.project.asset.domain.entity.AuditLog;
import com.project.asset.dto.audit.AuditLogDto;
import com.project.asset.repository.AuditLogRepository;
import com.project.asset.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public PageResponse<AuditLogDto> list(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        Page<AuditLogDto> dtoPage = auditLogRepository.findAll(pageable).map(this::toDto);
        return PageResponse.<AuditLogDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .build();
    }

    private AuditLogDto toDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .action(log.getAction())
                .entity(log.getEntity())
                .entityId(log.getEntityId())
                .detail(log.getDetail())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

