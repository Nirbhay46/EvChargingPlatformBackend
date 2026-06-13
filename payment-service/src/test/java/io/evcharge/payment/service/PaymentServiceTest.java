package io.evcharge.payment.service;

import io.evcharge.common.events.BookingCreatedEvent;
import io.evcharge.payment.domain.Payment;
import io.evcharge.payment.domain.PaymentStatus;
import io.evcharge.payment.repo.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository repo;
    @Mock StripeMockGateway stripe;
    @Mock KafkaTemplate<String, Object> kafka;
    @InjectMocks PaymentService service;

    private BookingCreatedEvent sampleEvent() {
        return new BookingCreatedEvent(10L, 1L, "u@x.io", 5L, "Hub",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                new BigDecimal("12.34"));
    }

    @Test
    void process_marks_success_and_publishes_events() {
        when(repo.findByBookingId(10L)).thenReturn(Optional.empty());
        when(repo.save(any(Payment.class))).thenAnswer(inv -> { Payment p = inv.getArgument(0); p.setId(99L); return p; });
        when(stripe.charge(eq(10L), any(), eq("USD")))
                .thenReturn(new StripeMockGateway.ChargeResult(true, "ch_x", null));
        when(kafka.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        Payment result = service.process(sampleEvent());

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.getStripeChargeId()).isEqualTo("ch_x");
        verify(kafka, atLeast(2)).send(anyString(), anyString(), any());
    }

    @Test
    void process_marks_failed_on_card_decline() {
        when(repo.findByBookingId(10L)).thenReturn(Optional.empty());
        when(repo.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stripe.charge(eq(10L), any(), eq("USD")))
                .thenReturn(new StripeMockGateway.ChargeResult(false, null, "card_declined"));
        when(kafka.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        Payment result = service.process(sampleEvent());

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getFailureReason()).isEqualTo("card_declined");
    }

    @Test
    void process_is_idempotent_per_booking() {
        Payment existing = Payment.builder().id(1L).bookingId(10L).status(PaymentStatus.SUCCESS).build();
        when(repo.findByBookingId(10L)).thenReturn(Optional.of(existing));

        Payment result = service.process(sampleEvent());

        assertThat(result).isSameAs(existing);
        verify(stripe, never()).charge(any(), any(), any());
        verify(kafka, never()).send(anyString(), anyString(), any());
    }
}
