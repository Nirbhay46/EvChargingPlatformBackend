package io.evcharge.common.events;

/**
 * Kafka topic names — shared between producers and consumers.
 */
public final class Topics {
    public static final String BOOKING_EVENTS      = "booking-events";
    public static final String PAYMENT_EVENTS      = "payment-events";
    public static final String NOTIFICATION_EVENTS = "notification-events";

    private Topics() {}
}
