package io.evcharge.booking.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

/**
 * Feign client to station-service with Resilience4j circuit breaker + retry.
 * Fallback returns null so booking-service can surface a friendly error.
 */
@FeignClient(name = "station-service", url = "${clients.station-service.url}",
        fallback = StationClient.Fallback.class)
public interface StationClient {

    @CircuitBreaker(name = "station", fallbackMethod = "fallback")
    @Retry(name = "station")
    @GetMapping("/api/stations/{id}")
    StationDto getStation(@PathVariable Long id);

    record StationDto(Long id, String name, String city, String address,
                      Double latitude, Double longitude,
                      Integer totalSlots, Integer powerKw,
                      String connectorType,
                      BigDecimal pricePerKwh, String currency,
                      String status) {}

    @Component
    class Fallback implements StationClient {
        @Override public StationDto getStation(Long id) { return null; }
    }
}
