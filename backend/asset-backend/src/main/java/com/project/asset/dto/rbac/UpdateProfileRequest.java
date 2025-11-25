package com.project.asset.dto.rbac;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String fullName;

    @Email(message = "邮箱格式不正确")
    private String email;
}

