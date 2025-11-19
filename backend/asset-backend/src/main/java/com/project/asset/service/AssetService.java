package com.project.asset.service;

import com.project.asset.domain.entity.Asset;
import com.project.asset.domain.entity.AssetType;
import com.project.asset.domain.entity.User;
import com.project.asset.domain.entity.Vendor;
import com.project.asset.domain.enums.AssetStatus;
import com.project.asset.dto.asset.AssetDto;
import com.project.asset.exception.BusinessException;
import com.project.asset.exception.ErrorCode;
import com.project.asset.repository.AssetRepository;
import com.project.asset.repository.AssetTypeRepository;
import com.project.asset.repository.UserRepository;
import com.project.asset.repository.VendorRepository;
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
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    public PageResponse<AssetDto> list(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        Page<AssetDto> dtoPage = assetRepository.findAll(pageable).map(this::toDto);
        return PageResponse.<AssetDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .build();
    }

    @Transactional
    public AssetDto create(AssetDto dto) {
        Asset asset = new Asset();
        apply(dto, asset);
        asset.setAssetNo(dto.getAssetNo());
        asset.setStatus(dto.getStatus() != null ? dto.getStatus() : AssetStatus.DRAFT);
        asset.setCreatedAt(LocalDateTime.now());
        return toDto(assetRepository.save(asset));
    }

    private void apply(AssetDto dto, Asset asset) {
        asset.setName(dto.getName());
        if (dto.getAssetTypeId() != null) {
            AssetType type = assetTypeRepository
                    .findById(dto.getAssetTypeId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "资产类型不存在"));
            asset.setAssetType(type);
        }
        if (dto.getVendorId() != null) {
            Vendor vendor = vendorRepository
                    .findById(dto.getVendorId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商不存在"));
            asset.setVendor(vendor);
        }
        if (dto.getCreatedBy() != null) {
            User creator = userRepository
                    .findById(dto.getCreatedBy())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
            asset.setCreatedBy(creator);
        }
        asset.setModel(dto.getModel());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setLocation(dto.getLocation());
        asset.setPrice(dto.getPrice());
        asset.setStatus(dto.getStatus() != null ? dto.getStatus() : AssetStatus.DRAFT);
    }

    private AssetDto toDto(Asset asset) {
        return AssetDto.builder()
                .id(asset.getId())
                .assetNo(asset.getAssetNo())
                .name(asset.getName())
                .assetTypeId(asset.getAssetType() != null ? asset.getAssetType().getId() : null)
                .model(asset.getModel())
                .vendorId(asset.getVendor() != null ? asset.getVendor().getId() : null)
                .purchaseDate(asset.getPurchaseDate())
                .status(asset.getStatus())
                .location(asset.getLocation())
                .price(asset.getPrice())
                .createdBy(asset.getCreatedBy() != null ? asset.getCreatedBy().getId() : null)
                .build();
    }
}

