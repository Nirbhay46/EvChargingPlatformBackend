package io.evcharge.booking.kafka;

import io.evcharge.common.events.BookingCreatedEvent;
import io.evcharge.common.events.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Retryable(retryFor = Exception.class, maxAttempts = 3,
               backoff = @Backoff(delay = 500, multiplier = 2))
    public void publishBookingCreated(BookingCreatedEvent event) {
        kafkaTemplate.send(Topics.BOOKING_EVENTS, String.valueOf(event.bookingId()), event)
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("Failed to publish BookingCreated {}", event.bookingId(), ex);
                    else log.debug("Published BookingCreated {}", event.bookingId());
                });
    }
}
