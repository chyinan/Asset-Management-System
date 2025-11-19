package com.project.asset.controller;

import com.project.asset.dto.rbac.RoleDto;
import com.project.asset.response.ApiResponse;
import com.project.asset.response.PageResponse;
import com.project.asset.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Role", description = "角色管理")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色")
    @PreAuthorize("hasAuthority('role:manage')")
    @GetMapping
    public ApiResponse<PageResponse<RoleDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        return ApiResponse.success(roleService.list(page, size, sort));
    }

    @Operation(summary = "创建角色")
    @PreAuthorize("hasAuthority('role:manage')")
    @PostMapping
    public ApiResponse<RoleDto> create(@Valid @RequestBody RoleDto dto) {
        return ApiResponse.success(roleService.create(dto));
    }

    @Operation(summary = "更新角色")
    @PreAuthorize("hasAuthority('role:manage')")
    @PutMapping("/{id}")
    public ApiResponse<RoleDto> update(@PathVariable Long id, @Valid @RequestBody RoleDto dto) {
        return ApiResponse.success(roleService.update(id, dto));
    }
}

