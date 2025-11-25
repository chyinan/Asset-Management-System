package com.project.asset.dto.system;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLoanReminderSettingRequest {

    @NotBlank(message = "发送邮箱不能为空")
    @Email(message = "请输入合法的邮箱地址")
    private String senderEmail;

    private String smtpHost;

    @Min(value = 1, message = "端口最小为1")
    @Max(value = 65535, message = "端口最大为65535")
    private Integer smtpPort;

    private String smtpUsername;

    private String smtpPassword;

    private Boolean smtpUseTls;
}

