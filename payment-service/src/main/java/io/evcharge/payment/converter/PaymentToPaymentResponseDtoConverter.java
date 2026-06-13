package io.evcharge.payment.converter;

import io.evcharge.payment.api.dto.PaymentResponseDto;
import io.evcharge.payment.domain.Payment;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PaymentToPaymentResponseDtoConverter implements Converter<Payment, PaymentResponseDto> {

    @Override
    public PaymentResponseDto convert(Payment p) {
        return new PaymentResponseDto(
                p.getId(), p.getBookingId(), p.getUserId(), p.getUserEmail(),
                p.getAmount(), p.getCurrency(), p.getStatus(),
                p.getStripeChargeId(), p.getFailureReason(), p.getCreatedAt()
        );
    }
}
