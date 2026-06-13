-- Envers revision info table
CREATE TABLE revinfo (
    rev      INT    NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT NOT NULL,
    PRIMARY KEY (rev)
);

-- Audit table for stations
CREATE TABLE stations_aud (
    id              BIGINT          NOT NULL,
    rev             INT             NOT NULL,
    revtype         TINYINT,
    name            VARCHAR(120),
    city            VARCHAR(80),
    address         VARCHAR(255),
    latitude        DOUBLE,
    longitude       DOUBLE,
    total_slots     INT,
    power_kw        INT,
    connector_type  VARCHAR(32),
    price_per_kwh   DECIMAL(10,4),
    currency        VARCHAR(8),
    status          VARCHAR(32),
    operator_id     BIGINT,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_stations_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
