package com.project.asset.service;

import com.project.asset.domain.entity.Asset;
import com.project.asset.domain.entity.CheckoutRecord;
import com.project.asset.domain.entity.Inventory;
import com.project.asset.domain.entity.User;
import com.project.asset.domain.enums.AssetStatus;
import com.project.asset.domain.enums.CheckoutType;
import com.project.asset.domain.enums.InventoryStatus;
import com.project.asset.dto.asset.CheckoutRequestDto;
import com.project.asset.dto.asset.InventoryDto;
import com.project.asset.dto.asset.ReturnRequestDto;
import com.project.asset.dto.asset.StockInRequestDto;
import com.project.asset.exception.BusinessException;
import com.project.asset.exception.ErrorCode;
import com.project.asset.repository.AssetRepository;
import com.project.asset.repository.CheckoutRecordRepository;
import com.project.asset.repository.InventoryRepository;
import com.project.asset.repository.UserRepository;
import com.project.asset.response.PageResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final AssetRepository assetRepository;
    private final CheckoutRecordRepository checkoutRecordRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public PageResponse<InventoryDto> list(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        Page<InventoryDto> dtoPage = inventoryRepository.findAll(pageable).map(this::toDto);
        return PageResponse.<InventoryDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .build();
    }

    @Transactional
    public Inventory stockIn(StockInRequestDto dto) {
        Asset asset = assetRepository
                .findById(dto.getAssetId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "资产不存在"));
        asset.setStatus(AssetStatus.IN_STOCK);
        Inventory inventory = new Inventory();
        inventory.setAsset(asset);
        inventory.setSerialNo(dto.getSerialNo());
        inventory.setLocation(dto.getLocation());
        inventory.setStatus(InventoryStatus.IN_STOCK);
        inventory.setCreatedAt(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());
        Inventory saved = inventoryRepository.save(inventory);
        auditService.record(asset.getCreatedBy() != null ? asset.getCreatedBy().getId() : null, "STOCK_IN", "Inventory", saved.getId(), dto.getSerialNo());
        return saved;
    }

    @Transactional
    public Inventory checkout(Long inventoryId, CheckoutRequestDto dto) {
        Inventory inventory = inventoryRepository
                .findById(inventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "库存不存在"));
        if (inventory.getStatus() != InventoryStatus.IN_STOCK) {
            throw new BusinessException(ErrorCode.CONFLICT, "资产当前不可领用");
        }
        User user = userRepository
                .findById(dto.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        inventory.setStatus(InventoryStatus.CHECKED_OUT);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
        writeCheckoutRecord(inventory, user, CheckoutType.CHECKOUT, dto.getRemark());
        auditService.record(user.getId(), "CHECKOUT", "Inventory", inventoryId, dto.getRemark());
        return inventory;
    }

    @Transactional
    public Inventory returnAsset(Long inventoryId, ReturnRequestDto dto) {
        Inventory inventory = inventoryRepository
                .findById(inventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "库存不存在"));
        if (inventory.getStatus() != InventoryStatus.CHECKED_OUT) {
            throw new BusinessException(ErrorCode.CONFLICT, "资产不在领用状态");
        }
        User user = userRepository
                .findById(dto.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        inventory.setStatus(InventoryStatus.IN_STOCK);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
        writeCheckoutRecord(inventory, user, CheckoutType.RETURN, dto.getRemark());
        auditService.record(user.getId(), "RETURN", "Inventory", inventoryId, dto.getRemark());
        return inventory;
    }

    private void writeCheckoutRecord(Inventory inventory, User user, CheckoutType type, String remark) {
        CheckoutRecord record = new CheckoutRecord();
        record.setInventory(inventory);
        record.setUser(user);
        record.setType(type);
        record.setRemark(remark);
        record.setCreatedAt(LocalDateTime.now());
        checkoutRecordRepository.save(record);
    }

    private InventoryDto toDto(Inventory inventory) {
        return InventoryDto.builder()
                .id(inventory.getId())
                .assetId(inventory.getAsset().getId())
                .assetName(inventory.getAsset().getName())
                .assetNo(inventory.getAsset().getAssetNo())
                .serialNo(inventory.getSerialNo())
                .status(inventory.getStatus())
                .location(inventory.getLocation())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}

