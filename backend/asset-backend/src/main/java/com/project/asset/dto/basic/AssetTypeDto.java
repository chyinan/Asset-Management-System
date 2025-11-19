package com.project.asset.dto.basic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "资产类型")
public class AssetTypeDto {

    private Long id;

    @NotBlank
    private String name;

    private String code;

    private String remark;
}


