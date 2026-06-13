package io.evcharge.booking.kafka;

import io.evcharge.booking.service.BookingService;
import io.evcharge.common.events.PaymentCompletedEvent;
import io.evcharge.common.events.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final BookingService bookingService;

    @KafkaListener(topics = Topics.PAYMENT_EVENTS, groupId = "booking-service",
            containerFactory = "paymentEventListenerFactory")
    public void onPaymentCompleted(PaymentCompletedEvent ev) {
        log.info("Received PaymentCompleted bookingId={} status={}", ev.bookingId(), ev.status());
        bookingService.applyPaymentResult(ev);
    }
}
