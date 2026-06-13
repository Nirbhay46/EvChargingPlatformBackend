-- Envers revision info table (shared across all audited entities in this service)
CREATE TABLE revinfo (
    rev      INT          NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT       NOT NULL,
    PRIMARY KEY (rev)
);

-- Audit table for bookings
CREATE TABLE bookings_aud (
    id               BIGINT         NOT NULL,
    rev              INT            NOT NULL,
    revtype          TINYINT,
    user_id          BIGINT,
    user_email       VARCHAR(120),
    station_id       BIGINT,
    station_name     VARCHAR(120),
    start_time       DATETIME(6),
    end_time         DATETIME(6),
    estimated_cost   DECIMAL(10,2),
    currency         VARCHAR(8),
    status           VARCHAR(32),
    idempotency_key  VARCHAR(64),
    version          BIGINT,
    created_at       DATETIME(6),
    updated_at       DATETIME(6),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_bookings_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
