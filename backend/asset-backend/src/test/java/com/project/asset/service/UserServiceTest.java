package com.project.asset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.project.asset.domain.entity.Department;
import com.project.asset.domain.entity.Permission;
import com.project.asset.domain.entity.Role;
import com.project.asset.domain.entity.User;
import com.project.asset.dto.rbac.UserDto;
import com.project.asset.exception.BusinessException;
import com.project.asset.repository.DepartmentRepository;
import com.project.asset.repository.PermissionRepository;
import com.project.asset.repository.RoleRepository;
import com.project.asset.repository.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Department department;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        departmentRepository.deleteAll();

        department = new Department();
        department.setName("IT");
        department = departmentRepository.save(department);

        Permission viewPerm = new Permission();
        viewPerm.setCode("asset:view");
        viewPerm.setName("View");
        viewPerm = permissionRepository.save(viewPerm);

        roleUser = new Role();
        roleUser.setCode("ROLE_USER");
        roleUser.setName("User");
        roleUser.setPermissions(Set.of(viewPerm));
        roleRepository.save(roleUser);
    }

    @Test
    void createUser_shouldEncodePasswordAndAssignRoles() {
        UserDto dto = UserDto.builder()
                .username("new_user")
                .password("Plain123")
                .fullName("New User")
                .email("user@example.com")
                .departmentId(department.getId())
                .roles(Set.of(roleUser.getCode()))
                .build();

        UserDto result = userService.create(dto);

        User saved = userRepository.findByUsername("new_user").orElseThrow();
        assertThat(passwordEncoder.matches("Plain123", saved.getPassword())).isTrue();
        assertThat(result.getRoles()).containsExactly(roleUser.getCode());
        assertThat(saved.getDepartment()).isNotNull();
    }

    @Test
    void createUser_whenUsernameExists_shouldThrowBusinessException() {
        User existing = new User();
        existing.setUsername("dup_user");
        existing.setPassword("encoded");
        existing.setRoles(Set.of(roleUser));
        userRepository.save(existing);

        UserDto dto = UserDto.builder()
                .username("dup_user")
                .password("test")
                .roles(Set.of(roleUser.getCode()))
                .build();

        assertThrows(BusinessException.class, () -> userService.create(dto));
    }
}

