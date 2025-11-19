package com.project.asset.dto.basic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "供应商信息")
public class VendorDto {

    private Long id;

    @NotBlank
    private String name;

    private String contact;

    private String phone;

    private String remark;
}


