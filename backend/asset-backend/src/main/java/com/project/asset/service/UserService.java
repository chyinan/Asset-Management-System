package com.project.asset.service;

import com.project.asset.domain.entity.Department;
import com.project.asset.domain.entity.Role;
import com.project.asset.domain.entity.User;
import com.project.asset.dto.rbac.UserDto;
import com.project.asset.exception.BusinessException;
import com.project.asset.exception.ErrorCode;
import com.project.asset.repository.DepartmentRepository;
import com.project.asset.repository.RoleRepository;
import com.project.asset.repository.UserRepository;
import com.project.asset.response.PageResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<UserDto> list(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<UserDto> dtoPage = userRepository.findAll(pageable).map(this::toDto);
        return PageResponse.<UserDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .build();
    }

    @Transactional
    public UserDto create(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        User user = new User();
        apply(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        apply(dto, user);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return toDto(userRepository.save(user));
    }

    private void apply(UserDto dto, User user) {
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : Boolean.TRUE);
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository
                    .findById(dto.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "部门不存在"));
            user.setDepartment(dept);
        } else {
            user.setDepartment(null);
        }
        if (dto.getRoles() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAll());
            user.setRoles(roles.stream()
                    .filter(role -> dto.getRoles().contains(role.getCode()))
                    .collect(Collectors.toSet()));
        }
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()))
                .build();
    }
}


