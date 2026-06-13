CREATE TABLE payments (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    booking_id       BIGINT        NOT NULL,
    user_id          BIGINT        NOT NULL,
    user_email       VARCHAR(120)  NOT NULL,
    amount           DECIMAL(10,2) NOT NULL,
    currency         VARCHAR(8)    NOT NULL DEFAULT 'USD',
    status           VARCHAR(32)   NOT NULL,
    stripe_charge_id VARCHAR(64)   NULL,
    failure_reason   VARCHAR(255)  NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_payments_booking UNIQUE (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_payments_booking ON payments (booking_id);
CREATE INDEX idx_payments_user    ON payments (user_id);
