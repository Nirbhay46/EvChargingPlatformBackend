package io.evcharge.common.events;

import java.util.Map;

/**
 * Generic notification request published to notification-service.
 */
public record NotificationEvent(
        String channel,           // EMAIL
        String recipient,         // email address
        String templateId,        // BOOKING_CONFIRMED | PAYMENT_RECEIPT | CHARGING_COMPLETE
        String subject,
        Map<String, String> data  // template placeholders
) {}
