package com.project.asset.service;

import com.project.asset.dto.rbac.PermissionDto;
import com.project.asset.repository.PermissionRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public List<PermissionDto> listAll() {
        return permissionRepository.findAll().stream()
                .map(permission -> PermissionDto.builder()
                        .id(permission.getId())
                        .code(permission.getCode())
                        .name(permission.getName())
                        .build())
                .collect(Collectors.toList());
    }
}


