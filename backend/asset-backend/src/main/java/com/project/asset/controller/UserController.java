package com.project.asset.controller;

import com.project.asset.dto.rbac.UpdateProfileRequest;
import com.project.asset.dto.rbac.UserDto;
import com.project.asset.dto.rbac.UserProfileDto;
import com.project.asset.response.ApiResponse;
import com.project.asset.response.PageResponse;
import com.project.asset.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.project.asset.security.UserPrincipal;

@Tag(name = "User", description = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasAuthority('user:manage')")
    @GetMapping
    public ApiResponse<PageResponse<UserDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        return ApiResponse.success(userService.list(page, size, sort));
    }

    @Operation(summary = "创建用户")
    @PreAuthorize("hasAuthority('user:manage')")
    @PostMapping
    public ApiResponse<UserDto> create(@Valid @RequestBody UserDto dto) {
        return ApiResponse.success(userService.create(dto));
    }

    @Operation(summary = "更新用户")
    @PreAuthorize("hasAuthority('user:manage')")
    @PutMapping("/{id}")
    public ApiResponse<UserDto> update(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        return ApiResponse.success(userService.update(id, dto));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public ApiResponse<UserProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(userService.getProfile(principal.getUserId()));
    }

    @Operation(summary = "更新当前用户信息")
    @PutMapping("/me")
    public ApiResponse<UserProfileDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(userService.updateProfile(principal.getUserId(), request));
    }
}

