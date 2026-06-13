package io.evcharge.booking.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Audited
@Entity
@Table(name = "bookings",
    indexes = {@Index(name = "idx_bookings_user", columnList = "user_id"),
               @Index(name = "idx_bookings_station_window", columnList = "station_id, start_time, end_time")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)   private Long userId;
    @Column(name = "user_email", nullable = false, length = 120) private String userEmail;
    @Column(name = "station_id", nullable = false) private Long stationId;
    @Column(name = "station_name", nullable = false, length = 120) private String stationName;

    @Column(name = "start_time", nullable = false) private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)   private LocalDateTime endTime;

    @Column(name = "estimated_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(nullable = false, length = 8) @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32) @Builder.Default
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;

    /** Idempotency token from client (optional) — prevents accidental dup submissions. */
    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    @Version
    private Long version;

    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp   @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
