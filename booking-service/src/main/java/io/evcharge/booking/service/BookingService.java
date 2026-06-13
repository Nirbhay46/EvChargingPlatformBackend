package io.evcharge.booking.service;

import io.evcharge.booking.api.dto.BookingRequest;
import io.evcharge.booking.api.dto.BookingResponse;
import io.evcharge.booking.client.StationClient;
import io.evcharge.booking.converter.BookingToBookingResponseDtoConverter;
import io.evcharge.booking.domain.Booking;
import io.evcharge.booking.domain.BookingStatus;
import io.evcharge.booking.exception.ApiException;
import io.evcharge.booking.kafka.BookingEventProducer;
import io.evcharge.booking.repo.BookingRepository;
import io.evcharge.common.events.BookingCreatedEvent;
import io.evcharge.common.events.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository repo;
    private final StationClient stationClient;
    private final BookingEventProducer producer;
    private final BookingToBookingResponseDtoConverter converter;

    @Transactional
    public BookingResponse create(BookingRequest req, Long userId, String userEmail) {
        validateWindow(req.startTime(), req.endTime());

        // Idempotency check
        if (req.idempotencyKey() != null) {
            var existing = repo.findByIdempotencyKey(req.idempotencyKey());
            if (existing.isPresent()) return converter.convert(existing.get());
        }

        StationClient.StationDto station = stationClient.getStation(req.stationId());
        if (station == null)
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Station service unavailable");
        if (!"ACTIVE".equals(station.status()))
            throw new ApiException(HttpStatus.CONFLICT, "Station is not active");

        // Double-booking / capacity check
        long overlapping = repo.countOverlapping(req.stationId(), req.startTime(), req.endTime());
        if (overlapping >= station.totalSlots()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No available slots for the requested time window");
        }

        BigDecimal cost = calculateCost(req.startTime(), req.endTime(),
                station.powerKw(), station.pricePerKwh());

        Booking b = Booking.builder()
                .userId(userId).userEmail(userEmail)
                .stationId(station.id()).stationName(station.name())
                .startTime(req.startTime()).endTime(req.endTime())
                .estimatedCost(cost).currency(station.currency())
                .status(BookingStatus.PENDING_PAYMENT)
                .idempotencyKey(req.idempotencyKey())
                .build();
        b = repo.save(b);

        producer.publishBookingCreated(new BookingCreatedEvent(
                b.getId(), userId, userEmail,
                station.id(), station.name(),
                b.getStartTime(), b.getEndTime(), cost));

        log.info("Booking created id={} user={} station={}", b.getId(), userId, station.id());
        return converter.convert(b);
    }

    @Transactional
    public void applyPaymentResult(PaymentCompletedEvent ev) {
        Booking b = repo.findById(ev.bookingId()).orElse(null);
        if (b == null) {
            log.warn("Booking {} not found while applying payment", ev.bookingId());
            return;
        }
        b.setStatus("SUCCESS".equals(ev.status())
                ? BookingStatus.CONFIRMED : BookingStatus.PAYMENT_FAILED);
        log.info("Booking {} -> {}", b.getId(), b.getStatus());
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, Long userId) {
        Booking b = repo.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!b.getUserId().equals(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "Cannot cancel another user's booking");
        if (b.getStatus() == BookingStatus.COMPLETED)
            throw new ApiException(HttpStatus.CONFLICT, "Completed bookings cannot be cancelled");
        b.setStatus(BookingStatus.CANCELLED);
        return converter.convert(b);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> listForUser(Long userId, Pageable pg) {
        return repo.findByUserIdOrderByStartTimeDesc(userId, pg).map(converter::convert);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id, Long userId) {
        Booking b = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!b.getUserId().equals(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        return converter.convert(b);
    }

    private void validateWindow(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start))
            throw new ApiException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        Duration d = Duration.between(start, end);
        if (d.toMinutes() < 15)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Minimum booking is 15 minutes");
        if (d.toHours() > 8)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Maximum booking is 8 hours");
    }

    /** Cost = power(kW) × duration(hours) × price/kWh */
    private BigDecimal calculateCost(LocalDateTime s, LocalDateTime e, int kw, BigDecimal pricePerKwh) {
        double hours = Duration.between(s, e).toMinutes() / 60.0;
        BigDecimal energy = BigDecimal.valueOf(kw * hours);
        return energy.multiply(pricePerKwh).setScale(2, RoundingMode.HALF_UP);
    }
}
