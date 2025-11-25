package com.project.asset.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "loan_reminder_settings")
public class LoanReminderSetting {

    @Id
    private Long id;

    @Column(name = "sender_email", nullable = false, length = 200)
    private String senderEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "smtp_host", length = 200)
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "smtp_username", length = 200)
    private String smtpUsername;

    @Column(name = "smtp_password", length = 200)
    private String smtpPassword;

    @Column(name = "smtp_use_tls")
    private Boolean smtpUseTls = Boolean.FALSE;
}

