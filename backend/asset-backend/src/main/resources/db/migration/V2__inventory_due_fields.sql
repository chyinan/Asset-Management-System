ALTER TABLE inventory
    ADD COLUMN current_holder_id BIGINT NULL,
    ADD COLUMN checked_out_at TIMESTAMP NULL,
    ADD COLUMN expected_return_at TIMESTAMP NULL,
    ADD COLUMN last_reminder_at TIMESTAMP NULL,
    ADD COLUMN reminder_count INT DEFAULT 0;

ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_current_holder FOREIGN KEY (current_holder_id) REFERENCES users(id);




