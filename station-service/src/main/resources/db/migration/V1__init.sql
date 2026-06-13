CREATE TABLE stations (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    name            VARCHAR(120)  NOT NULL,
    city            VARCHAR(80)   NOT NULL,
    address         VARCHAR(255)  NOT NULL,
    latitude        DOUBLE        NOT NULL,
    longitude       DOUBLE        NOT NULL,
    total_slots     INT           NOT NULL,
    power_kw        INT           NOT NULL,
    connector_type  VARCHAR(32)   NOT NULL,
    price_per_kwh   DECIMAL(10,4) NOT NULL,
    currency        VARCHAR(8)    NOT NULL DEFAULT 'USD',
    status          VARCHAR(32)   NOT NULL DEFAULT 'ACTIVE',
    operator_id     BIGINT        NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_stations_city   ON stations (city);
CREATE INDEX idx_stations_status ON stations (status);

-- Seed a few stations for demo
INSERT INTO stations (name, city, address, latitude, longitude, total_slots, power_kw,
                      connector_type, price_per_kwh, currency, status)
VALUES
  ('Downtown SuperCharger', 'Seattle',  '500 Pine St',     47.6131, -122.3389,  8, 150, 'CCS',   0.3200, 'USD', 'ACTIVE'),
  ('Eastside FastCharge',   'Bellevue', '101 NE 8th St',   47.6172, -122.1933,  4,  50, 'TYPE2', 0.2500, 'USD', 'ACTIVE'),
  ('Airport Hub',           'SeaTac',   '17801 Intl Blvd', 47.4502, -122.3088, 12, 250, 'TESLA', 0.4000, 'USD', 'ACTIVE');
