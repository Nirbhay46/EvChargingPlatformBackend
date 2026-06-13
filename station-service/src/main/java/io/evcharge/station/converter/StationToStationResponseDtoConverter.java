package io.evcharge.station.converter;

import io.evcharge.station.api.dto.StationResponse;
import io.evcharge.station.domain.Station;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StationToStationResponseDtoConverter implements Converter<Station, StationResponse> {

    @Override
    public StationResponse convert(Station s) {
        return new StationResponse(
                s.getId(), s.getName(), s.getCity(), s.getAddress(),
                s.getLatitude(), s.getLongitude(),
                s.getTotalSlots(), s.getPowerKw(),
                s.getConnectorType(), s.getPricePerKwh(),
                s.getCurrency(), s.getStatus()
        );
    }
}
