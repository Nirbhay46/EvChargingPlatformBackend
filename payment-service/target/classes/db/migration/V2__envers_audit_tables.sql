-- Envers revision info table
CREATE TABLE revinfo (
    rev      INT    NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT NOT NULL,
    PRIMARY KEY (rev)
);

-- Audit table for payments
CREATE TABLE payments_aud (
    id                BIGINT         NOT NULL,
    rev               INT            NOT NULL,
    revtype           TINYINT,
    booking_id        BIGINT,
    user_id           BIGINT,
    user_email        VARCHAR(120),
    amount            DECIMAL(10,2),
    currency          VARCHAR(8),
    status            VARCHAR(32),
    stripe_charge_id  VARCHAR(64),
    failure_reason    VARCHAR(255),
    created_at        DATETIME(6),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_payments_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
