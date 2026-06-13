package io.evcharge.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Audited
@Entity
@Table(name = "payments",
    indexes = {@Index(name = "idx_payments_booking", columnList = "booking_id")},
    uniqueConstraints = @UniqueConstraint(name = "uk_payments_booking", columnNames = "booking_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false) private Long bookingId;
    @Column(name = "user_id", nullable = false)    private Long userId;
    @Column(name = "user_email", nullable = false, length = 120) private String userEmail;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 8) @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "stripe_charge_id", length = 64)
    private String stripeChargeId;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
