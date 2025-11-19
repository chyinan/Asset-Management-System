package com.project.asset.controller;

import com.project.asset.dto.basic.VendorDto;
import com.project.asset.response.ApiResponse;
import com.project.asset.response.PageResponse;
import com.project.asset.service.VendorService;
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

@Tag(name = "Vendor", description = "供应商管理")
@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @Operation(summary = "分页查询供应商")
    @PreAuthorize("hasAuthority('asset:view')")
    @GetMapping
    public ApiResponse<PageResponse<VendorDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        return ApiResponse.success(vendorService.list(page, size, sort));
    }

    @Operation(summary = "创建供应商")
    @PreAuthorize("hasAuthority('asset:admin')")
    @PostMapping
    public ApiResponse<VendorDto> create(@Valid @RequestBody VendorDto dto) {
        return ApiResponse.success(vendorService.create(dto));
    }

    @Operation(summary = "更新供应商")
    @PreAuthorize("hasAuthority('asset:admin')")
    @PutMapping("/{id}")
    public ApiResponse<VendorDto> update(@PathVariable Long id, @Valid @RequestBody VendorDto dto) {
        return ApiResponse.success(vendorService.update(id, dto));
    }
}


