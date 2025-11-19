package com.project.asset.service;

import com.project.asset.domain.entity.Vendor;
import com.project.asset.dto.basic.VendorDto;
import com.project.asset.exception.BusinessException;
import com.project.asset.exception.ErrorCode;
import com.project.asset.repository.VendorRepository;
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
public class VendorService {

    private final VendorRepository vendorRepository;

    public PageResponse<VendorDto> list(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<VendorDto> dtoPage = vendorRepository.findAll(pageable).map(this::toDto);
        return PageResponse.<VendorDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .build();
    }

    @Transactional
    public VendorDto create(VendorDto dto) {
        Vendor vendor = new Vendor();
        apply(dto, vendor);
        return toDto(vendorRepository.save(vendor));
    }

    @Transactional
    public VendorDto update(Long id, VendorDto dto) {
        Vendor vendor = vendorRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商不存在"));
        apply(dto, vendor);
        return toDto(vendorRepository.save(vendor));
    }

    private void apply(VendorDto dto, Vendor vendor) {
        vendor.setName(dto.getName());
        vendor.setContact(dto.getContact());
        vendor.setPhone(dto.getPhone());
        vendor.setRemark(dto.getRemark());
    }

    private VendorDto toDto(Vendor vendor) {
        return VendorDto.builder()
                .id(vendor.getId())
                .name(vendor.getName())
                .contact(vendor.getContact())
                .phone(vendor.getPhone())
                .remark(vendor.getRemark())
                .build();
    }
}


