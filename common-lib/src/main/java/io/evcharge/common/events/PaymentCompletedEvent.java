package io.evcharge.common.events;

import java.math.BigDecimal;

/**
 * Published by payment-service after attempting payment.
 * Consumed by booking-service (to confirm/cancel) and notification-service.
 */
public record PaymentCompletedEvent(
        Long bookingId,
        Long userId,
        String userEmail,
        Long paymentId,
        BigDecimal amount,
        String currency,
        String status,        // SUCCESS | FAILED
        String stripeChargeId // mock
) {}
