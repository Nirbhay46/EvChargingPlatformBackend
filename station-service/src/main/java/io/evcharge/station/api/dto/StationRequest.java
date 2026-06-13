package io.evcharge.station.api.dto;

import io.evcharge.station.domain.ConnectorType;
import io.evcharge.station.domain.StationStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record StationRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 80)  String city,
        @NotBlank @Size(max = 255) String address,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")   Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @NotNull @Min(1) @Max(50)  Integer totalSlots,
        @NotNull @Min(3) @Max(400) Integer powerKw,
        @NotNull ConnectorType connectorType,
        @NotNull @DecimalMin("0.0001") BigDecimal pricePerKwh,
        @NotBlank @Size(min = 3, max = 3) String currency,
        StationStatus status
) {}
