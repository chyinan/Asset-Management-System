package com.project.asset.dto.audit;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditLogDto {
    private Long id;
    private Long userId;
    private String action;
    private String entity;
    private Long entityId;
    private String detail;
    private LocalDateTime createdAt;
}

