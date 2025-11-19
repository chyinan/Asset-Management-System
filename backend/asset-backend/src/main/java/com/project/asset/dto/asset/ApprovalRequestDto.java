package com.project.asset.dto.asset;

import com.project.asset.domain.enums.ApprovalResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "审批请求")
public class ApprovalRequestDto {

    @NotNull
    private Long approverId;

    @NotNull
    private ApprovalResult result;

    private String comment;
}

