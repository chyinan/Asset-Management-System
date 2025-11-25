package com.project.asset.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(description = "库存领用请求")
public class CheckoutRequestDto {
    @NotNull
    private Long userId;

    private String remark;

    @Future(message = "预计归还时间需为未来时间")
    @Schema(description = "预计归还时间，未填写则按默认天数计算")
    private LocalDateTime expectedReturnAt;
}

