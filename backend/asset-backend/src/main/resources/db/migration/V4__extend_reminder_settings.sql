ALTER TABLE loan_reminder_settings
ADD COLUMN smtp_host VARCHAR(200),
ADD COLUMN smtp_port INT,
ADD COLUMN smtp_username VARCHAR(200),
ADD COLUMN smtp_password VARCHAR(200),
ADD COLUMN smtp_use_tls BOOLEAN DEFAULT FALSE;

