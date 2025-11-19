package com.project.asset.security;

import com.project.asset.config.JwtProperties;
import com.project.asset.domain.entity.Permission;
import com.project.asset.domain.entity.Role;
import com.project.asset.domain.entity.User;
import com.project.asset.dto.auth.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;
    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            try {
                keyBytes = MessageDigest.getInstance("SHA-256").digest(keyBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Unable to initialize JWT signing key", e);
            }
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenResponse createTokenResponse(User user) {
        String accessToken = buildToken(user, properties.getAccessTokenExpiration(), TokenType.ACCESS);
        String refreshToken = buildToken(user, properties.getRefreshTokenExpiration(), TokenType.REFRESH);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .expiresIn(properties.getAccessTokenExpiration())
                .refreshToken(refreshToken)
                .refreshExpiresIn(properties.getRefreshTokenExpiration())
                .build();
    }

    private String buildToken(User user, long expirySeconds, TokenType tokenType) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirySeconds);
        Set<String> roles = user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("type", tokenType.name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
    }

    public boolean isAccessToken(String token) {
        return TokenType.ACCESS.name().equalsIgnoreCase(parseClaims(token).get("type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return TokenType.REFRESH.name().equalsIgnoreCase(parseClaims(token).get("type", String.class));
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String extractUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }
}

