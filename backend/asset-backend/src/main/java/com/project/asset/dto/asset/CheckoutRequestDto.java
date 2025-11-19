package com.project.asset.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "库存领用请求")
public class CheckoutRequestDto {
    @NotNull
    private Long userId;

    private String remark;
}

