package com.project.asset.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "入库请求")
public class StockInRequestDto {

    @NotNull
    private Long assetId;

    @NotBlank
    private String serialNo;

    private String location;
}

