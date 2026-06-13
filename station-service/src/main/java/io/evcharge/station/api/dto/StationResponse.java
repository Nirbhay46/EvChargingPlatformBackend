package io.evcharge.station.api.dto;

import io.evcharge.station.domain.ConnectorType;
import io.evcharge.station.domain.StationStatus;

import java.math.BigDecimal;

public record StationResponse(
        Long id, String name, String city, String address,
        Double latitude, Double longitude,
        Integer totalSlots, Integer powerKw,
        ConnectorType connectorType,
        BigDecimal pricePerKwh, String currency,
        StationStatus status
) {}
