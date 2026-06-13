package io.evcharge.booking.api.dto;

import io.evcharge.booking.domain.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id, Long userId, String userEmail,
        Long stationId, String stationName,
        LocalDateTime startTime, LocalDateTime endTime,
        BigDecimal estimatedCost, String currency,
        BookingStatus status,
        LocalDateTime createdAt
) {}
