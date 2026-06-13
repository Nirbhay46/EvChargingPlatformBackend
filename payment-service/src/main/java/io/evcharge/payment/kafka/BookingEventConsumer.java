package io.evcharge.payment.kafka;

import io.evcharge.common.events.BookingCreatedEvent;
import io.evcharge.common.events.Topics;
import io.evcharge.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = Topics.BOOKING_EVENTS, groupId = "payment-service",
            containerFactory = "bookingEventListenerFactory")
    public void onBookingCreated(BookingCreatedEvent ev) {
        log.info("Received BookingCreated bookingId={} amount={}", ev.bookingId(), ev.estimatedCost());
        paymentService.process(ev);
    }
}
