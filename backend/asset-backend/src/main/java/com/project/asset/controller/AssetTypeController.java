package com.project.asset.controller;

import com.project.asset.dto.basic.AssetTypeDto;
import com.project.asset.response.ApiResponse;
import com.project.asset.response.PageResponse;
import com.project.asset.service.AssetTypeService;
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

@Tag(name = "AssetType", description = "资产类型管理")
@RestController
@RequestMapping("/api/asset-types")
@RequiredArgsConstructor
public class AssetTypeController {

    private final AssetTypeService assetTypeService;

    @Operation(summary = "分页查询资产类型")
    @PreAuthorize("hasAuthority('asset:view')")
    @GetMapping
    public ApiResponse<PageResponse<AssetTypeDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        return ApiResponse.success(assetTypeService.list(page, size, sort));
    }

    @Operation(summary = "创建资产类型")
    @PreAuthorize("hasAuthority('asset:admin')")
    @PostMapping
    public ApiResponse<AssetTypeDto> create(@Valid @RequestBody AssetTypeDto dto) {
        return ApiResponse.success(assetTypeService.create(dto));
    }

    @Operation(summary = "更新资产类型")
    @PreAuthorize("hasAuthority('asset:admin')")
    @PutMapping("/{id}")
    public ApiResponse<AssetTypeDto> update(@PathVariable Long id, @Valid @RequestBody AssetTypeDto dto) {
        return ApiResponse.success(assetTypeService.update(id, dto));
    }
}


