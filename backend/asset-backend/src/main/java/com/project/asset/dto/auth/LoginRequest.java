package com.project.asset.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Schema(description = "用户名", example = "admin")
    @NotBlank
    private String username;

    @Schema(description = "密码", example = "admin123")
    @NotBlank
    private String password;
}


