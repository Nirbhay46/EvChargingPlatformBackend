package io.evcharge.notification.service;

import java.util.Map;

/**
 * Trivial template engine — replaces {{key}} placeholders.
 * For real use, swap to FreeMarker / Mustache / Thymeleaf.
 */
public final class TemplateRenderer {

    private TemplateRenderer() {}

    public static String render(String tpl, Map<String, String> data) {
        String out = tpl;
        if (data != null) {
            for (var e : data.entrySet()) {
                out = out.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
            }
        }
        return out;
    }

    public static final Map<String, String> TEMPLATES = Map.of(
        "BOOKING_CONFIRMED", """
            Hi {{fullName}},

            Your booking at {{stationName}} is CONFIRMED.

            • From: {{startTime}}
            • To:   {{endTime}}
            • Amount charged: {{amount}} {{currency}}
            • Stripe charge: {{chargeId}}

            Drive safe and enjoy your charge!
            — EV Charge Team
            """,
        "PAYMENT_FAILED", """
            Hi {{fullName}},

            Unfortunately your payment for the booking at {{stationName}} FAILED.
            Reason: {{reason}}

            Please update your payment method and try again.

            — EV Charge Team
            """,
        "CHARGING_COMPLETE", """
            Hi {{fullName}},

            Your charging session at {{stationName}} is complete.
            Thank you for choosing EV Charge.
            """
    );
}
