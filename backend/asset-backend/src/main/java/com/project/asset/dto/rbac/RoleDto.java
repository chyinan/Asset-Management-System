package com.project.asset.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "角色信息")
public class RoleDto {
    private Long id;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String remark;

    @NotEmpty
    private Set<String> permissions;
}


