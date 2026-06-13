package io.evcharge.booking.converter;

import io.evcharge.booking.api.dto.BookingResponse;
import io.evcharge.booking.domain.Booking;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BookingToBookingResponseDtoConverter implements Converter<Booking, BookingResponse> {

    @Override
    public BookingResponse convert(Booking b) {
        return new BookingResponse(
                b.getId(), b.getUserId(), b.getUserEmail(),
                b.getStationId(), b.getStationName(),
                b.getStartTime(), b.getEndTime(),
                b.getEstimatedCost(), b.getCurrency(),
                b.getStatus(), b.getCreatedAt()
        );
    }
}
