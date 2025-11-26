ALTER TABLE loan_reminder_settings
ADD COLUMN reminder_start_days INT DEFAULT 7,
ADD COLUMN reminder_cron VARCHAR(100) DEFAULT '0 0 9 ? * MON';

