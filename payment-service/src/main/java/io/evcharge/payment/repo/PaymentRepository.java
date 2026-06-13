package io.evcharge.payment.repo;

import io.evcharge.payment.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pg);
}
