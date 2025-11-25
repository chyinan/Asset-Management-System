CREATE TABLE IF NOT EXISTS loan_reminder_settings (
    id BIGINT PRIMARY KEY,
    sender_email VARCHAR(200) NOT NULL,
    updated_by BIGINT NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_loan_reminder_settings_user FOREIGN KEY (updated_by) REFERENCES users(id)
);

INSERT INTO loan_reminder_settings (id, sender_email, updated_at)
VALUES (1, 'no-reply@asset.local', NOW())
ON DUPLICATE KEY UPDATE sender_email = VALUES(sender_email);

