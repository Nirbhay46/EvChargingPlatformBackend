package io.evcharge.payment.service;

import io.evcharge.common.events.*;
import io.evcharge.payment.domain.Payment;
import io.evcharge.payment.domain.PaymentStatus;
import io.evcharge.payment.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repo;
    private final StripeMockGateway stripe;
    private final KafkaTemplate<String, Object> kafka;

    @Transactional
    public Payment process(BookingCreatedEvent ev) {
        // Idempotency: don't re-process
        var existing = repo.findByBookingId(ev.bookingId());
        if (existing.isPresent()) {
            log.info("Payment for booking {} already processed", ev.bookingId());
            return existing.get();
        }

        Payment p = Payment.builder()
                .bookingId(ev.bookingId()).userId(ev.userId()).userEmail(ev.userEmail())
                .amount(ev.estimatedCost()).currency("USD")
                .status(PaymentStatus.PENDING).build();
        p = repo.save(p);

        StripeMockGateway.ChargeResult result = stripe.charge(ev.bookingId(),
                ev.estimatedCost(), "USD");

        if (result.success()) {
            p.setStatus(PaymentStatus.SUCCESS);
            p.setStripeChargeId(result.chargeId());
        } else {
            p.setStatus(PaymentStatus.FAILED);
            p.setFailureReason(result.failureReason());
        }
        repo.save(p);

        publish(p, ev);
        return p;
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 5,
               backoff = @Backoff(delay = 500, multiplier = 2, maxDelay = 5000))
    void publish(Payment p, BookingCreatedEvent ev) {
        PaymentCompletedEvent payEv = new PaymentCompletedEvent(
                p.getBookingId(), p.getUserId(), p.getUserEmail(),
                p.getId(), p.getAmount(), p.getCurrency(),
                p.getStatus().name(), p.getStripeChargeId());
        kafka.send(Topics.PAYMENT_EVENTS, String.valueOf(p.getBookingId()), payEv);

        // Trigger notification
        NotificationEvent notify = new NotificationEvent(
                "EMAIL", p.getUserEmail(),
                p.getStatus() == PaymentStatus.SUCCESS ? "BOOKING_CONFIRMED" : "PAYMENT_FAILED",
                p.getStatus() == PaymentStatus.SUCCESS
                        ? "Booking confirmed at " + ev.stationName()
                        : "Payment failed for your booking",
                Map.of(
                        "fullName", p.getUserEmail(),
                        "stationName", ev.stationName(),
                        "startTime", String.valueOf(ev.startTime()),
                        "endTime", String.valueOf(ev.endTime()),
                        "amount", String.valueOf(p.getAmount()),
                        "currency", p.getCurrency(),
                        "chargeId", String.valueOf(p.getStripeChargeId()),
                        "reason", String.valueOf(p.getFailureReason())));
        kafka.send(Topics.NOTIFICATION_EVENTS, String.valueOf(p.getBookingId()), notify);
    }
}
