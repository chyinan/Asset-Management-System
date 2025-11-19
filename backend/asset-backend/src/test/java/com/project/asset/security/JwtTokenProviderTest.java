package com.project.asset.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.asset.config.JwtProperties;
import com.project.asset.domain.entity.Permission;
import com.project.asset.domain.entity.Role;
import com.project.asset.domain.entity.User;
import com.project.asset.dto.auth.TokenResponse;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void createTokenResponse_shouldContainClaimsAndBeValid() {
        Permission permission = new Permission();
        permission.setCode("asset:view");
        permission.setName("view");

        Role role = new Role();
        role.setCode("ROLE_ADMIN");
        role.setName("Admin");
        role.setPermissions(Set.of(permission));

        User user = new User();
        user.setId(99L);
        user.setUsername("jwt-user");
        user.setRoles(Set.of(role));

        TokenResponse response = jwtTokenProvider.createTokenResponse(user);

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getExpiresIn()).isEqualTo(jwtProperties.getAccessTokenExpiration());
        assertThat(jwtTokenProvider.isTokenValid(response.getAccessToken())).isTrue();
        assertThat(jwtTokenProvider.extractUsername(response.getAccessToken())).isEqualTo("jwt-user");
        assertThat(jwtTokenProvider.isAccessToken(response.getAccessToken())).isTrue();
        assertThat(jwtTokenProvider.isRefreshToken(response.getRefreshToken())).isTrue();
    }
}

