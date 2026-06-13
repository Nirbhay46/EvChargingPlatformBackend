package io.evcharge.payment.api.dto;

import io.evcharge.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long id,
        Long bookingId,
        Long userId,
        String userEmail,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String stripeChargeId,
        String failureReason,
        LocalDateTime createdAt
) {}
