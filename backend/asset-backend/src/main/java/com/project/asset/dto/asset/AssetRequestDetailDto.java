package com.project.asset.dto.asset;

import com.project.asset.domain.enums.RequestStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetRequestDetailDto {
    private Long id;
    private String requestNo;
    private Long requesterId;
    private Long departmentId;
    private RequestStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private List<RequestItemDto> items;
}

