package com.project.asset.service;

import com.project.asset.config.LoanReminderProperties;
import com.project.asset.domain.entity.Inventory;
import com.project.asset.domain.entity.LoanReminderSetting;
import com.project.asset.domain.entity.User;
import com.project.asset.domain.enums.InventoryStatus;
import com.project.asset.repository.InventoryRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
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
    private final TaskScheduler taskScheduler;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ScheduledFuture<?> currentTask;

    @PostConstruct
    public void init() {
        refreshSchedule();
    }

    public void refreshSchedule() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(false);
        }

        String cron = loanReminderSettingService.getSettingEntity()
                .map(LoanReminderSetting::getReminderCron)
                .filter(StringUtils::hasText)
                .orElse(loanReminderProperties.getReminderCron());

        log.info("Scheduling loan reminder task with cron: {}", cron);
        try {
            currentTask = taskScheduler.schedule(this::sendLoanReminders, new CronTrigger(cron));
        } catch (Exception e) {
            log.error("Failed to schedule loan reminder task", e);
        }
    }

    @Transactional
    public void sendLoanReminders() {
        if (!loanReminderProperties.isReminderEnabled()) {
            return;
        }
        JavaMailSender mailSender =
                loanReminderSettingService.determineMailSender(mailSenderProvider.getIfAvailable());
        if (mailSender == null) {
            log.warn("Mail sender is not configured, skip reminder job");
            return;
        }

        Integer startDays = loanReminderSettingService.getSettingEntity()
                .map(LoanReminderSetting::getReminderStartDays)
                .orElse(7);

        LocalDateTime now = LocalDateTime.now();
        // Find items that are checked out and (expected_return <= now + startDays)
        // This covers overdue items (expected_return < now) and items approaching due date.
        LocalDateTime threshold = now.plusDays(startDays);
        
        List<Inventory> candidates = inventoryRepository.findByStatusAndExpectedReturnAtBefore(
                InventoryStatus.CHECKED_OUT, threshold);

        if (candidates.isEmpty()) {
            return;
        }

        List<Inventory> notified = new ArrayList<>();
        for (Inventory inventory : candidates) {
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
            log.info("Loan reminder sent for {} records", notified.size());
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
        // Cooldown check
        return lastReminderAt.isBefore(now.minusDays(loanReminderProperties.getReminderCooldownDays()));
    }

    private boolean sendReminder(Inventory inventory, JavaMailSender mailSender) {
        try {
            User holder = inventory.getCurrentHolder();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(holder.getEmail());
            message.setFrom(loanReminderSettingService.resolveSenderEmail());
            
            String subject = "资产归还提醒";
            if (inventory.getExpectedReturnAt().isBefore(LocalDateTime.now())) {
                subject = "资产逾期提醒";
            }
            
            message.setSubject(subject + " - " + inventory.getAsset().getName());
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
        
        String statusDesc = "即将到期";
        if (inventory.getExpectedReturnAt().isBefore(LocalDateTime.now())) {
            statusDesc = "已逾期";
        }

        return String.format(
                """
                %s 您好，
                
                您领用的资产%s，请尽快办理归还或与资产管理员确认。
                
                资产名称：%s
                资产编号：%s
                序列号：%s
                预计归还时间：%s
                
                如果已归还，请忽略本邮件。
                
                —— 资产管理系统
                """,
                holderName,
                statusDesc,
                inventory.getAsset().getName(),
                inventory.getAsset().getAssetNo(),
                inventory.getSerialNo(),
                expectedReturn);
    }
}
