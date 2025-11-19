package com.project.asset.service;

import com.project.asset.domain.entity.Department;
import com.project.asset.dto.basic.DepartmentDto;
import com.project.asset.exception.BusinessException;
import com.project.asset.exception.ErrorCode;
import com.project.asset.repository.DepartmentRepository;
import com.project.asset.repository.UserRepository;
import com.project.asset.response.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public PageResponse<DepartmentDto> list(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<DepartmentDto> dtoPage = departmentRepository.findAll(pageable).map(this::toDto);
        return PageResponse.<DepartmentDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .build();
    }

    @Transactional
    public DepartmentDto create(DepartmentDto dto) {
        Department department = new Department();
        apply(dto, department);
        return toDto(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department department = departmentRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "部门不存在"));
        apply(dto, department);
        return toDto(departmentRepository.save(department));
    }

    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "部门不存在"));
        if (departmentRepository.existsByParent_Id(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "存在下级部门，无法删除");
        }
        if (userRepository.existsByDepartment_Id(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "存在用户关联该部门，无法删除");
        }
        departmentRepository.delete(department);
    }

    public DepartmentDto findById(Long id) {
        return departmentRepository
                .findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));
    }

    private void apply(DepartmentDto dto, Department department) {
        department.setName(dto.getName());
        department.setRemark(dto.getRemark());
        if (dto.getParentId() != null) {
            Optional<Department> parent = departmentRepository.findById(dto.getParentId());
            parent.ifPresent(department::setParent);
        } else {
            department.setParent(null);
        }
    }

    private DepartmentDto toDto(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .remark(department.getRemark())
                .parentId(department.getParent() != null ? department.getParent().getId() : null)
                .build();
    }
}


