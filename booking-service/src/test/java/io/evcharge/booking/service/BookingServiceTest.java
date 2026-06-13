package io.evcharge.booking.service;

import io.evcharge.booking.api.dto.BookingRequest;
import io.evcharge.booking.client.StationClient;
import io.evcharge.booking.domain.Booking;
import io.evcharge.booking.domain.BookingStatus;
import io.evcharge.booking.exception.ApiException;
import io.evcharge.booking.kafka.BookingEventProducer;
import io.evcharge.booking.repo.BookingRepository;
import io.evcharge.common.events.PaymentCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository repo;
    @Mock StationClient stationClient;
    @Mock BookingEventProducer producer;
    @InjectMocks BookingService service;

    private StationClient.StationDto activeStation(int slots) {
        return new StationClient.StationDto(10L, "Hub", "Seattle", "addr",
                47.0, -122.0, slots, 150, "CCS",
                new BigDecimal("0.30"), "USD", "ACTIVE");
    }

    @Test
    void create_succeeds_when_slots_available() {
        var start = LocalDateTime.now().plusDays(1);
        var end   = start.plusHours(1);
        when(stationClient.getStation(10L)).thenReturn(activeStation(4));
        when(repo.countOverlapping(eq(10L), any(), any())).thenReturn(2L);
        when(repo.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0); b.setId(99L); return b;
        });

        var res = service.create(new BookingRequest(10L, start, end, null), 5L, "u@x.io");

        assertThat(res.id()).isEqualTo(99L);
        assertThat(res.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        // 150 kW * 1h * 0.30 = 45.00
        assertThat(res.estimatedCost()).isEqualByComparingTo("45.00");
        verify(producer).publishBookingCreated(any());
    }

    @Test
    void create_rejects_when_no_slots_available() {
        var start = LocalDateTime.now().plusDays(1);
        var end   = start.plusHours(1);
        when(stationClient.getStation(10L)).thenReturn(activeStation(2));
        when(repo.countOverlapping(eq(10L), any(), any())).thenReturn(2L); // all full

        assertThatThrownBy(() ->
                service.create(new BookingRequest(10L, start, end, null), 5L, "u@x.io"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No available slots");
        verify(producer, never()).publishBookingCreated(any());
    }

    @Test
    void create_rejects_invalid_window_too_short() {
        var start = LocalDateTime.now().plusDays(1);
        var end   = start.plusMinutes(5);
        assertThatThrownBy(() ->
                service.create(new BookingRequest(10L, start, end, null), 5L, "u@x.io"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Minimum booking");
    }

    @Test
    void idempotent_create_returns_existing() {
        Booking existing = Booking.builder().id(123L).userId(5L).userEmail("u@x.io")
                .stationId(10L).stationName("Hub")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .estimatedCost(new BigDecimal("45.00")).currency("USD")
                .status(BookingStatus.CONFIRMED).build();
        when(repo.findByIdempotencyKey("abc")).thenReturn(Optional.of(existing));

        var res = service.create(new BookingRequest(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                "abc"), 5L, "u@x.io");

        assertThat(res.id()).isEqualTo(123L);
        verify(stationClient, never()).getStation(any());
    }

    @Test
    void applyPaymentResult_marks_confirmed_on_success() {
        Booking b = Booking.builder().id(50L).status(BookingStatus.PENDING_PAYMENT).build();
        when(repo.findById(50L)).thenReturn(Optional.of(b));

        service.applyPaymentResult(new PaymentCompletedEvent(50L, 5L, "u@x.io",
                100L, new BigDecimal("45.00"), "USD", "SUCCESS", "ch_x"));

        assertThat(b.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }
}
