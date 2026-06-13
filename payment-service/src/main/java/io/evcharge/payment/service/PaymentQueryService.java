package io.evcharge.payment.service;

import io.evcharge.payment.api.dto.PaymentResponseDto;
import io.evcharge.payment.converter.PaymentToPaymentResponseDtoConverter;
import io.evcharge.payment.exception.ApiException;
import io.evcharge.payment.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    private final PaymentRepository repo;
    private final PaymentToPaymentResponseDtoConverter converter;

    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> listForUser(Long userId, Pageable pg) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId, pg).map(converter::convert);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto findByBooking(Long bookingId) {
        return repo.findByBookingId(bookingId)
                .map(converter::convert)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
    }
}
