package com.project.asset.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "创建资产申请请求")
public class AssetRequestCreateDto {

    @NotNull
    private Long requesterId;

    private Long departmentId;

    private String remark;

    @Valid
    private List<RequestItemDto> items;
}

