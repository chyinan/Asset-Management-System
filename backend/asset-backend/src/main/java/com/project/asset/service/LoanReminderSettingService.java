package com.project.asset.service;

import com.project.asset.config.LoanReminderProperties;
import com.project.asset.domain.entity.LoanReminderSetting;
import com.project.asset.domain.entity.User;
import com.project.asset.dto.system.LoanReminderSettingDto;
import com.project.asset.dto.system.UpdateLoanReminderSettingRequest;
import com.project.asset.repository.LoanReminderSettingRepository;
import com.project.asset.repository.UserRepository;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanReminderSettingService {

    private static final Long SINGLETON_ID = 1L;

    private final LoanReminderSettingRepository settingRepository;
    private final UserRepository userRepository;
    private final LoanReminderProperties loanReminderProperties;

    public LoanReminderSettingDto getSettings() {
        return toDto(settingRepository.findById(SINGLETON_ID).orElse(null));
    }

    @Transactional
    public LoanReminderSettingDto updateSettings(UpdateLoanReminderSettingRequest request, @Nullable Long operatorId) {
        LoanReminderSetting setting = settingRepository
                .findById(SINGLETON_ID)
                .orElseGet(this::createSetting);
        setting.setSenderEmail(request.getSenderEmail());
        setting.setSmtpHost(request.getSmtpHost());
        setting.setSmtpPort(request.getSmtpPort());
        setting.setSmtpUsername(request.getSmtpUsername());
        if (StringUtils.hasText(request.getSmtpPassword())) {
            setting.setSmtpPassword(request.getSmtpPassword());
        }
        setting.setSmtpUseTls(Boolean.TRUE.equals(request.getSmtpUseTls()));
        setting.setUpdatedAt(LocalDateTime.now());
        if (operatorId != null) {
            userRepository.findById(operatorId).ifPresent(setting::setUpdatedBy);
        } else {
            setting.setUpdatedBy(null);
        }
        settingRepository.save(setting);
        return toDto(setting);
    }

    public String resolveSenderEmail() {
        return settingRepository
                .findById(SINGLETON_ID)
                .map(LoanReminderSetting::getSenderEmail)
                .filter(StringUtils::hasText)
                .orElse(loanReminderProperties.getReminderEmailFrom());
    }

    public JavaMailSender determineMailSender(@Nullable JavaMailSender defaultSender) {
        LoanReminderSetting setting = settingRepository.findById(SINGLETON_ID).orElse(null);
        if (setting == null || !hasCustomSmtp(setting)) {
            return defaultSender;
        }
        if (!StringUtils.hasText(setting.getSmtpHost())
                || setting.getSmtpPort() == null
                || !StringUtils.hasText(setting.getSmtpUsername())
                || !StringUtils.hasText(setting.getSmtpPassword())) {
            return defaultSender;
        }
        JavaMailSenderImpl custom = new JavaMailSenderImpl();
        custom.setHost(setting.getSmtpHost());
        custom.setPort(setting.getSmtpPort());
        custom.setUsername(setting.getSmtpUsername());
        custom.setPassword(setting.getSmtpPassword());
        custom.setDefaultEncoding("UTF-8");
        Properties props = custom.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", Boolean.TRUE.equals(setting.getSmtpUseTls()) ? "true" : "false");
        props.put("mail.smtp.ssl.enable", Boolean.TRUE.equals(setting.getSmtpUseTls()) ? "true" : "false");
        return custom;
    }

    private LoanReminderSetting createSetting() {
        LoanReminderSetting created = new LoanReminderSetting();
        created.setId(SINGLETON_ID);
        created.setSmtpUseTls(Boolean.FALSE);
        return created;
    }

    private boolean hasCustomSmtp(LoanReminderSetting setting) {
        return StringUtils.hasText(setting.getSmtpHost())
                || setting.getSmtpPort() != null
                || StringUtils.hasText(setting.getSmtpUsername())
                || StringUtils.hasText(setting.getSmtpPassword());
    }

    private LoanReminderSettingDto toDto(@Nullable LoanReminderSetting setting) {
        if (setting == null) {
            return LoanReminderSettingDto.builder()
                    .senderEmail(loanReminderProperties.getReminderEmailFrom())
                    .smtpUseTls(Boolean.FALSE)
                    .build();
        }
        return LoanReminderSettingDto.builder()
                .senderEmail(setting.getSenderEmail())
                .updatedAt(setting.getUpdatedAt())
                .updatedBy(Optional.ofNullable(setting.getUpdatedBy())
                        .map(this::resolveUserName)
                        .orElse(null))
                .smtpHost(setting.getSmtpHost())
                .smtpPort(setting.getSmtpPort())
                .smtpUsername(setting.getSmtpUsername())
                .smtpUseTls(Boolean.TRUE.equals(setting.getSmtpUseTls()))
                .build();
    }

    private String resolveUserName(User user) {
        if (StringUtils.hasText(user.getFullName())) {
            return user.getFullName();
        }
        return user.getUsername();
    }
}

