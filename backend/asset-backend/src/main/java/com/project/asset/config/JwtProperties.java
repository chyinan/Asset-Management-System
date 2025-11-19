package com.project.asset.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /**
     * HMAC secret or RSA private key depending on signing strategy.
     */
    private String secret = "please-change-me";

    /**
     * Access token expiration seconds.
     */
    private long accessTokenExpiration = 3600;

    /**
     * Refresh token expiration seconds.
     */
    private long refreshTokenExpiration = 604800;
}


