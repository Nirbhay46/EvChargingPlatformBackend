-- Envers revision info table
CREATE TABLE revinfo (
    rev      INT    NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT NOT NULL,
    PRIMARY KEY (rev)
);

-- Audit table for notifications
CREATE TABLE notifications_aud (
    id                  BIGINT        NOT NULL,
    rev                 INT           NOT NULL,
    revtype             TINYINT,
    channel             VARCHAR(32),
    recipient           VARCHAR(120),
    template_id         VARCHAR(64),
    subject             VARCHAR(200),
    body                TEXT,
    status              VARCHAR(32),
    provider_message_id VARCHAR(100),
    error_message       VARCHAR(500),
    created_at          DATETIME(6),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_notifications_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
