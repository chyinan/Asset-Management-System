package com.project.asset.service;

import com.project.asset.domain.entity.User;
import com.project.asset.dto.auth.LoginRequest;
import com.project.asset.dto.auth.TokenResponse;
import com.project.asset.exception.BusinessException;
import com.project.asset.exception.ErrorCode;
import com.project.asset.repository.UserRepository;
import com.project.asset.security.JwtTokenProvider;
import com.project.asset.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return jwtTokenProvider.createTokenResponse(principal.user());
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.isTokenValid(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }
        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        User user = userRepository
                .findById(userId)
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或已禁用"));
        return jwtTokenProvider.createTokenResponse(user);
    }
}

