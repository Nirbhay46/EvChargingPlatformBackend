CREATE TABLE notifications (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    channel             VARCHAR(32)  NOT NULL,
    recipient           VARCHAR(120) NOT NULL,
    template_id         VARCHAR(64)  NOT NULL,
    subject             VARCHAR(200) NOT NULL,
    body                TEXT         NULL,
    status              VARCHAR(32)  NOT NULL,
    provider_message_id VARCHAR(100) NULL,
    error_message       VARCHAR(500) NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_recipient ON notifications (recipient);
