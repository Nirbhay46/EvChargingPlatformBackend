package io.evcharge.payment.service;

import io.evcharge.payment.domain.Payment;
import io.evcharge.payment.domain.PaymentStatus;
import io.evcharge.payment.exception.ApiException;
import io.evcharge.payment.repo.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentQueryServiceTest {

    @Mock PaymentRepository repo;
    @InjectMocks PaymentQueryService service;

    @Test
    void listForUser_delegatesToRepo() {
        Payment p = Payment.builder().id(1L).bookingId(10L).userId(5L)
                .userEmail("u@x.io").amount(BigDecimal.TEN).currency("USD")
                .status(PaymentStatus.SUCCESS).build();
        Page<Payment> page = new PageImpl<>(List.of(p));
        when(repo.findByUserIdOrderByCreatedAtDesc(eq(5L), any())).thenReturn(page);

        var res = service.listForUser(5L, PageRequest.of(0, 10));

        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getBookingId()).isEqualTo(10L);
    }

    @Test
    void findByBooking_returnsPayment() {
        Payment p = Payment.builder().id(1L).bookingId(10L).userId(5L)
                .userEmail("u@x.io").amount(BigDecimal.TEN).currency("USD")
                .status(PaymentStatus.SUCCESS).build();
        when(repo.findByBookingId(10L)).thenReturn(Optional.of(p));

        assertThat(service.findByBooking(10L).getId()).isEqualTo(1L);
    }

    @Test
    void findByBooking_throwsNotFound() {
        when(repo.findByBookingId(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByBooking(99L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not found");
    }
}
