package com.project.asset.dto.basic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "部门信息")
public class DepartmentDto {

    private Long id;

    @NotBlank
    private String name;

    private Long parentId;

    private String remark;
}


