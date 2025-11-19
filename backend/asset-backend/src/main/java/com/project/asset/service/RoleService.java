package com.project.asset.service;

import com.project.asset.domain.entity.Permission;
import com.project.asset.domain.entity.Role;
import com.project.asset.dto.rbac.RoleDto;
import com.project.asset.exception.BusinessException;
import com.project.asset.exception.ErrorCode;
import com.project.asset.repository.PermissionRepository;
import com.project.asset.repository.RoleRepository;
import com.project.asset.response.PageResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public PageResponse<RoleDto> list(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<RoleDto> dtoPage = roleRepository.findAll(pageable).map(this::toDto);
        return PageResponse.<RoleDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .build();
    }

    @Transactional
    public RoleDto create(RoleDto dto) {
        Role role = new Role();
        apply(dto, role);
        return toDto(roleRepository.save(role));
    }

    @Transactional
    public RoleDto update(Long id, RoleDto dto) {
        Role role = roleRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "角色不存在"));
        apply(dto, role);
        return toDto(roleRepository.save(role));
    }

    private void apply(RoleDto dto, Role role) {
        role.setCode(dto.getCode());
        role.setName(dto.getName());
        role.setRemark(dto.getRemark());
        if (dto.getPermissions() != null) {
            List<Permission> permissions = permissionRepository.findByCodeIn(dto.getPermissions());
            if (permissions.size() != dto.getPermissions().size()) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "权限编码不存在");
            }
            role.setPermissions(new HashSet<>(permissions));
        } else {
            role.setPermissions(new HashSet<>());
        }
    }

    private RoleDto toDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .remark(role.getRemark())
                .permissions(role.getPermissions().stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toSet()))
                .build();
    }
}


