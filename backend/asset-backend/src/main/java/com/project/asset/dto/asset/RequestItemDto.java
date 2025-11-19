package com.project.asset.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "资产申请项")
public class RequestItemDto {
    private Long id;

    @NotNull
    private Long assetTypeId;

    @Min(1)
    private Integer quantity;

    private String purpose;
}

