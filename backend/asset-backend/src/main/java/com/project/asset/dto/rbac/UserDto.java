package com.project.asset.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "用户信息")
public class UserDto {
    private Long id;

    @NotBlank
    private String username;

    private String fullName;

    @Email
    private String email;

    private Long departmentId;

    private Boolean enabled;

    private Set<String> roles;

    private String password;
}


