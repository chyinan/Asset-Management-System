package com.project.asset.controller;

import com.project.asset.dto.asset.AssetDto;
import com.project.asset.response.ApiResponse;
import com.project.asset.response.PageResponse;
import com.project.asset.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Asset", description = "资产主数据管理")
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @Operation(summary = "分页查询资产")
    @PreAuthorize("hasAuthority('asset:view')")
    @GetMapping
    public ApiResponse<PageResponse<AssetDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        return ApiResponse.success(assetService.list(page, size, sort));
    }

    @Operation(summary = "创建资产")
    @PreAuthorize("hasAuthority('asset:admin')")
    @PostMapping
    public ApiResponse<AssetDto> create(@Valid @RequestBody AssetDto dto) {
        return ApiResponse.success(assetService.create(dto));
    }
}

