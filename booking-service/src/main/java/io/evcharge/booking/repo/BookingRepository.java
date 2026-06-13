package io.evcharge.booking.repo;

import io.evcharge.booking.domain.Booking;
import io.evcharge.booking.domain.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdempotencyKey(String key);

    Page<Booking> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pg);

    /**
     * Count overlapping ACTIVE bookings for a station in a window.
     * Two windows [a,b) and [c,d) overlap when a < d AND c < b.
     */
    @Query("""
       SELECT COUNT(b) FROM Booking b
        WHERE b.stationId = :stationId
          AND b.status IN (io.evcharge.booking.domain.BookingStatus.PENDING_PAYMENT,
                           io.evcharge.booking.domain.BookingStatus.CONFIRMED)
          AND b.startTime < :endTime
          AND :startTime < b.endTime
       """)
    long countOverlapping(Long stationId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("""
       SELECT b FROM Booking b
        WHERE b.stationId = :stationId
          AND b.status IN (io.evcharge.booking.domain.BookingStatus.PENDING_PAYMENT,
                           io.evcharge.booking.domain.BookingStatus.CONFIRMED)
          AND b.startTime < :endTime
          AND :startTime < b.endTime
       """)
    List<Booking> findOverlapping(Long stationId, LocalDateTime startTime, LocalDateTime endTime);
}
