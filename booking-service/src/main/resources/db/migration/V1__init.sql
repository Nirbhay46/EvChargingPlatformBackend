CREATE TABLE bookings (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    user_id         BIGINT        NOT NULL,
    user_email      VARCHAR(120)  NOT NULL,
    station_id      BIGINT        NOT NULL,
    station_name    VARCHAR(120)  NOT NULL,
    start_time      TIMESTAMP     NOT NULL,
    end_time        TIMESTAMP     NOT NULL,
    estimated_cost  DECIMAL(10,2) NOT NULL,
    currency        VARCHAR(8)    NOT NULL DEFAULT 'USD',
    status          VARCHAR(32)   NOT NULL DEFAULT 'PENDING_PAYMENT',
    idempotency_key VARCHAR(64)   NULL,
    version         BIGINT        NOT NULL DEFAULT 0,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_bookings_idempotency UNIQUE (idempotency_key),
    CONSTRAINT chk_bookings_window CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bookings_user           ON bookings (user_id);
CREATE INDEX idx_bookings_station_window ON bookings (station_id, start_time, end_time);
CREATE INDEX idx_bookings_status         ON bookings (status);
