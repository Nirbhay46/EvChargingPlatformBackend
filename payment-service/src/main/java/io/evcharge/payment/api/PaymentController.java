package io.evcharge.payment.api;

import io.evcharge.payment.api.dto.PaymentResponseDto;
import io.evcharge.payment.service.PaymentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentQueryService paymentQueryService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public Page<PaymentResponseDto> myPayments(@RequestHeader("X-User-Id") Long userId, Pageable pg) {
        return paymentQueryService.listForUser(userId, pg);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/booking/{bookingId}")
    public PaymentResponseDto byBooking(@PathVariable Long bookingId) {
        return paymentQueryService.findByBooking(bookingId);
    }
}
