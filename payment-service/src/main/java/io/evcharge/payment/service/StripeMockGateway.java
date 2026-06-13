package io.evcharge.payment.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock Stripe gateway — deterministic-ish: succeeds ~95% of the time.
 * Returns a fake charge id (`ch_mock_<uuid>`). NO real network calls, NO real keys.
 * Wrapped with bulkhead + retry to demonstrate resilience patterns.
 */
@Slf4j
@Component
public class StripeMockGateway {

    public record ChargeResult(boolean success, String chargeId, String failureReason) {}

    @Bulkhead(name = "stripe")
    @Retry(name = "stripe")
    public ChargeResult charge(Long bookingId, BigDecimal amount, String currency) {
        log.info("Charging mock-Stripe amount={} {} for booking={}", amount, currency, bookingId);

        // Simulate network latency
        try { Thread.sleep(ThreadLocalRandom.current().nextInt(50, 250)); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

        // 5% simulated failure rate
        if (ThreadLocalRandom.current().nextInt(100) < 5) {
            return new ChargeResult(false, null, "card_declined (mock)");
        }
        return new ChargeResult(true, "ch_mock_" + UUID.randomUUID(), null);
    }
}
