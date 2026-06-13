package io.evcharge.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published by booking-service when a slot is reserved.
 * Consumed by payment-service.
 */
public record BookingCreatedEvent(
        Long bookingId,
        Long userId,
        String userEmail,
        Long stationId,
        String stationName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal estimatedCost
) {}
