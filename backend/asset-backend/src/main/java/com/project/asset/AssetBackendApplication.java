package com.project.asset;

import com.project.asset.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class AssetBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetBackendApplication.class, args);
    }
}
