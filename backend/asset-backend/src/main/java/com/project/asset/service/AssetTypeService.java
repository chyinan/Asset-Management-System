package com.project.asset.service;

import com.project.asset.domain.entity.AssetType;
import com.project.asset.dto.basic.AssetTypeDto;
import com.project.asset.exception.BusinessException;
import com.project.asset.exception.ErrorCode;
import com.project.asset.repository.AssetRepository;
import com.project.asset.repository.AssetTypeRepository;
import com.project.asset.response.PageResponse;
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
public class AssetTypeService {

    private final AssetTypeRepository assetTypeRepository;
    private final AssetRepository assetRepository;

    public PageResponse<AssetTypeDto> list(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<AssetTypeDto> dtoPage = assetTypeRepository.findAll(pageable).map(this::toDto);
        return PageResponse.<AssetTypeDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .build();
    }

    @Transactional
    public AssetTypeDto create(AssetTypeDto dto) {
        if (dto.getCode() != null && assetTypeRepository.existsByCode(dto.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "资产类型编码已存在");
        }
        AssetType assetType = new AssetType();
        apply(dto, assetType);
        return toDto(assetTypeRepository.save(assetType));
    }

    @Transactional
    public AssetTypeDto update(Long id, AssetTypeDto dto) {
        AssetType assetType = assetTypeRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "资产类型不存在"));
        if (dto.getCode() != null && assetTypeRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "资产类型编码已存在");
        }
        apply(dto, assetType);
        return toDto(assetTypeRepository.save(assetType));
    }

    @Transactional
    public void delete(Long id) {
        AssetType assetType = assetTypeRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "资产类型不存在"));
        if (assetRepository.existsByAssetType_Id(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "存在资产引用该类型，无法删除");
        }
        assetTypeRepository.delete(assetType);
    }

    private void apply(AssetTypeDto dto, AssetType assetType) {
        assetType.setName(dto.getName());
        assetType.setCode(dto.getCode());
        assetType.setRemark(dto.getRemark());
    }

    private AssetTypeDto toDto(AssetType assetType) {
        return AssetTypeDto.builder()
                .id(assetType.getId())
                .name(assetType.getName())
                .code(assetType.getCode())
                .remark(assetType.getRemark())
                .build();
    }
}


