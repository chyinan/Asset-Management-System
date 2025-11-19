package com.project.asset.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "权限信息")
public class PermissionDto {
    private Long id;
    private String code;
    private String name;
}


