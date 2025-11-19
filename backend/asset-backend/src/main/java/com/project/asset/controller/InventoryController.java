package com.project.asset.controller;

import com.project.asset.domain.entity.Inventory;
import com.project.asset.dto.asset.CheckoutRequestDto;
import com.project.asset.dto.asset.InventoryDto;
import com.project.asset.dto.asset.ReturnRequestDto;
import com.project.asset.dto.asset.StockInRequestDto;
import com.project.asset.response.ApiResponse;
import com.project.asset.response.PageResponse;
import com.project.asset.service.InventoryService;
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

@Tag(name = "Inventory", description = "库存管理")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "分页查询库存")
    @PreAuthorize("hasAuthority('asset:view')")
    @GetMapping
    public ApiResponse<PageResponse<InventoryDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort) {
        return ApiResponse.success(inventoryService.list(page, size, sort));
    }

    @Operation(summary = "资产入库")
    @PreAuthorize("hasAuthority('asset:stockin')")
    @PostMapping("/stock-in")
    public ApiResponse<Inventory> stockIn(@Valid @RequestBody StockInRequestDto dto) {
        return ApiResponse.success(inventoryService.stockIn(dto));
    }

    @Operation(summary = "库存领用")
    @PreAuthorize("hasAuthority('asset:checkout')")
    @PostMapping("/{id}/checkout")
    public ApiResponse<Inventory> checkout(
            @PathVariable Long id, @Valid @RequestBody CheckoutRequestDto dto) {
        return ApiResponse.success(inventoryService.checkout(id, dto));
    }

    @Operation(summary = "库存归还")
    @PreAuthorize("hasAuthority('asset:return')")
    @PostMapping("/{id}/return")
    public ApiResponse<Inventory> returnAsset(
            @PathVariable Long id, @Valid @RequestBody ReturnRequestDto dto) {
        return ApiResponse.success(inventoryService.returnAsset(id, dto));
    }
}

