package io.evcharge.station.service;

import io.evcharge.station.api.dto.StationRequest;
import io.evcharge.station.api.dto.StationResponse;
import io.evcharge.station.converter.StationToStationResponseDtoConverter;
import io.evcharge.station.domain.Station;
import io.evcharge.station.domain.StationStatus;
import io.evcharge.station.exception.ApiException;
import io.evcharge.station.repo.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository repo;
    private final StationToStationResponseDtoConverter converter;

    @Transactional
    @CacheEvict(value = "stations", allEntries = true)
    public StationResponse create(StationRequest req, Long operatorId) {
        Station s = Station.builder()
                .name(req.name()).city(req.city()).address(req.address())
                .latitude(req.latitude()).longitude(req.longitude())
                .totalSlots(req.totalSlots()).powerKw(req.powerKw())
                .connectorType(req.connectorType()).pricePerKwh(req.pricePerKwh())
                .currency(req.currency().toUpperCase())
                .status(req.status() != null ? req.status() : StationStatus.ACTIVE)
                .operatorId(operatorId)
                .build();
        return converter.convert(repo.save(s));
    }

    @Transactional
    @CacheEvict(value = "stations", key = "#id")
    public StationResponse update(Long id, StationRequest req) {
        Station s = repo.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Station not found"));
        s.setName(req.name()); s.setCity(req.city()); s.setAddress(req.address());
        s.setLatitude(req.latitude()); s.setLongitude(req.longitude());
        s.setTotalSlots(req.totalSlots()); s.setPowerKw(req.powerKw());
        s.setConnectorType(req.connectorType()); s.setPricePerKwh(req.pricePerKwh());
        s.setCurrency(req.currency().toUpperCase());
        if (req.status() != null) s.setStatus(req.status());
        return converter.convert(s);
    }

    /**
     * Hot read-path — called by booking-service via Feign for every booking.
     * Cached for 60s in Redis (see CacheConfig).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "stations", key = "#id", unless = "#result == null")
    public StationResponse get(Long id) {
        log.debug("Cache miss — loading station {} from DB", id);
        return repo.findById(id).map(converter::convert)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Station not found"));
    }

    @Transactional(readOnly = true)
    public Page<StationResponse> search(String city, StationStatus status, Pageable pg) {
        // Pageable params make caching of paginated lists complex — left uncached.
        return repo.search(city, status, pg).map(converter::convert);
    }

    @Transactional
    @CacheEvict(value = "stations", key = "#id")
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ApiException(HttpStatus.NOT_FOUND, "Station not found");
        repo.deleteById(id);
    }
}
