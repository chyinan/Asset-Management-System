package com.project.asset.dto.asset;

import com.project.asset.domain.enums.AssetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "资产信息")
public class AssetDto {
    private Long id;
    private String assetNo;
    private String name;
    private Long assetTypeId;
    private String model;
    private Long vendorId;
    private LocalDate purchaseDate;
    private AssetStatus status;
    private String location;
    private BigDecimal price;
    private Long createdBy;
}

