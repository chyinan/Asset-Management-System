package com.project.asset.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "asset.loan")
public class LoanReminderProperties {

    private boolean reminderEnabled = true;

    @Min(1)
    private int defaultDurationDays = 30;

    private String reminderCron = "0 0 9 ? * MON";

    @Min(1)
    private int reminderCooldownDays = 7;

    private String reminderEmailFrom = "no-reply@asset.local";
}



