package com.project.asset.service;

import com.project.asset.config.LoanReminderProperties;
import com.project.asset.domain.entity.Inventory;
import com.project.asset.domain.entity.User;
import com.project.asset.domain.enums.InventoryStatus;
import com.project.asset.repository.InventoryRepository;
import com.project.asset.service.LoanReminderSettingService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanReminderService {

    private final InventoryRepository inventoryRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final LoanReminderProperties loanReminderProperties;
    private final LoanReminderSettingService loanReminderSettingService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Scheduled(cron = "${asset.loan.reminder-cron:0 0 9 ? * MON}")
    @Transactional
    public void sendOverdueReminders() {
        if (!loanReminderProperties.isReminderEnabled()) {
            return;
        }
        JavaMailSender mailSender =
                loanReminderSettingService.determineMailSender(mailSenderProvider.getIfAvailable());
        if (mailSender == null) {
            log.warn("Mail sender is not configured, skip reminder job");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<Inventory> overdueInventories = inventoryRepository.findByStatusAndExpectedReturnAtBefore(
                InventoryStatus.CHECKED_OUT, now);

        if (overdueInventories.isEmpty()) {
            return;
        }

        List<Inventory> notified = new ArrayList<>();
        for (Inventory inventory : overdueInventories) {
            if (!shouldNotify(inventory, now)) {
                continue;
            }
            if (sendReminder(inventory, mailSender)) {
                inventory.setLastReminderAt(now);
                inventory.setReminderCount(
                        (inventory.getReminderCount() == null ? 0 : inventory.getReminderCount()) + 1);
                notified.add(inventory);
            }
        }
        if (!notified.isEmpty()) {
            inventoryRepository.saveAll(notified);
            log.info("Loan reminder sent for {} overdue records", notified.size());
        }
    }

    private boolean shouldNotify(Inventory inventory, LocalDateTime now) {
        User holder = inventory.getCurrentHolder();
        if (holder == null || !StringUtils.hasText(holder.getEmail())) {
            return false;
        }
        if (inventory.getExpectedReturnAt() == null) {
            return false;
        }
        LocalDateTime lastReminderAt = inventory.getLastReminderAt();
        if (lastReminderAt == null) {
            return true;
        }
        return lastReminderAt.isBefore(now.minusDays(loanReminderProperties.getReminderCooldownDays()));
    }

    private boolean sendReminder(Inventory inventory, JavaMailSender mailSender) {
        try {
            User holder = inventory.getCurrentHolder();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(holder.getEmail());
            message.setFrom(loanReminderSettingService.resolveSenderEmail());
            message.setSubject("资产归还提醒 - " + inventory.getAsset().getName());
            message.setText(buildBody(inventory));
            mailSender.send(message);
            return true;
        } catch (MailException ex) {
            log.error("Failed to send reminder for inventory {}: {}", inventory.getId(), ex.getMessage());
            return false;
        }
    }

    private String buildBody(Inventory inventory) {
        User holder = inventory.getCurrentHolder();
        String holderName = holder.getFullName() != null ? holder.getFullName() : holder.getUsername();
        String expectedReturn = inventory.getExpectedReturnAt().format(FORMATTER);
        return String.format(
                """
                %s 您好，

                您领用的资产已超过预定归还时间，请尽快办理归还或与资产管理员确认。

                资产名称：%s
                资产编号：%s
                序列号：%s
                预计归还时间：%s

                如果已归还，请忽略本邮件。

                —— 资产管理系统
                """,
                holderName,
                inventory.getAsset().getName(),
                inventory.getAsset().getAssetNo(),
                inventory.getSerialNo(),
                expectedReturn);
    }
}


