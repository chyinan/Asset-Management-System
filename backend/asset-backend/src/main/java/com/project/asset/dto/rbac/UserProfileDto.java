package com.project.asset.dto.rbac;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfileDto {
    Long id;
    String username;
    String fullName;
    String email;
}

