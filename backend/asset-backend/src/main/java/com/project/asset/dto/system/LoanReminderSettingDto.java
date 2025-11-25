package com.project.asset.dto.system;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoanReminderSettingDto {
    String senderEmail;
    String updatedBy;
    LocalDateTime updatedAt;
    String smtpHost;
    Integer smtpPort;
    String smtpUsername;
    Boolean smtpUseTls;
}

