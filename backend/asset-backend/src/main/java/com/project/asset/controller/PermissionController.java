package com.project.asset.controller;

import com.project.asset.dto.rbac.PermissionDto;
import com.project.asset.response.ApiResponse;
import com.project.asset.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Permission", description = "权限管理")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "列出所有权限")
    @PreAuthorize("hasAuthority('permission:view')")
    @GetMapping
    public ApiResponse<List<PermissionDto>> list() {
        return ApiResponse.success(permissionService.listAll());
    }

    @Operation(summary = "列出全部权限编码")
    @PreAuthorize("hasAnyAuthority('permission:view','role:manage')")
    @GetMapping("/codes")
    public ApiResponse<List<String>> listCodes() {
        return ApiResponse.success(permissionService.listCodes());
    }
}
