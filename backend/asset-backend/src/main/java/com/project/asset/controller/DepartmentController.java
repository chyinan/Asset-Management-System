package com.project.asset.controller;

import com.project.asset.dto.basic.DepartmentDto;
import com.project.asset.response.ApiResponse;
import com.project.asset.response.PageResponse;
import com.project.asset.service.DepartmentService;
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

@Tag(name = "Department", description = "部门管理")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "分页查询部门")
    @PreAuthorize("hasAuthority('asset:view')")
    @GetMapping
    public ApiResponse<PageResponse<DepartmentDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        return ApiResponse.success(departmentService.list(page, size, sort));
    }

    @Operation(summary = "获取部门详情")
    @PreAuthorize("hasAuthority('asset:view')")
    @GetMapping("/{id}")
    public ApiResponse<DepartmentDto> detail(@PathVariable Long id) {
        return ApiResponse.success(departmentService.findById(id));
    }

    @Operation(summary = "创建部门")
    @PreAuthorize("hasAuthority('asset:admin')")
    @PostMapping
    public ApiResponse<DepartmentDto> create(@Valid @RequestBody DepartmentDto dto) {
        return ApiResponse.success(departmentService.create(dto));
    }

    @Operation(summary = "更新部门")
    @PreAuthorize("hasAuthority('asset:admin')")
    @PutMapping("/{id}")
    public ApiResponse<DepartmentDto> update(@PathVariable Long id, @Valid @RequestBody DepartmentDto dto) {
        return ApiResponse.success(departmentService.update(id, dto));
    }
}


