package com.project.asset;

import com.project.asset.config.JwtProperties;
import com.project.asset.config.LoanReminderProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, LoanReminderProperties.class})
public class AssetBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetBackendApplication.class, args);
    }
}
