package com.project.asset.controller;

import com.project.asset.dto.asset.ApprovalRequestDto;
import com.project.asset.dto.asset.AssetRequestCreateDto;
import com.project.asset.dto.asset.AssetRequestDetailDto;
import com.project.asset.response.ApiResponse;
import com.project.asset.response.PageResponse;
import com.project.asset.service.AssetRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AssetRequest", description = "资产申请管理")
@RestController
@RequestMapping("/api/asset-requests")
@RequiredArgsConstructor
public class AssetRequestController {

    private final AssetRequestService assetRequestService;

    @Operation(summary = "分页查询资产申请")
    @PreAuthorize("hasAuthority('asset:view')")
    @GetMapping
    public ApiResponse<PageResponse<AssetRequestDetailDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        return ApiResponse.success(assetRequestService.list(page, size, sort));
    }

    @Operation(summary = "创建资产申请")
    @PreAuthorize("hasAuthority('asset:apply')")
    @PostMapping
    public ApiResponse<AssetRequestDetailDto> create(@Valid @RequestBody AssetRequestCreateDto dto) {
        return ApiResponse.success(assetRequestService.createRequest(dto));
    }

    @Operation(summary = "获取申请详情")
    @PreAuthorize("hasAuthority('asset:view')")
    @GetMapping("/{id}")
    public ApiResponse<AssetRequestDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.success(assetRequestService.getDetail(id));
    }

    @Operation(summary = "审批资产申请")
    @PreAuthorize("hasAuthority('asset:approve')")
    @PostMapping("/{id}/approve")
    public ApiResponse<AssetRequestDetailDto> approve(
            @PathVariable Long id, @Valid @RequestBody ApprovalRequestDto dto) {
        return ApiResponse.success(assetRequestService.approve(id, dto));
    }
}

