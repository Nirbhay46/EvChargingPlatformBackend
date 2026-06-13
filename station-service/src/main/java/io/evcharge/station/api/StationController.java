package io.evcharge.station.api;

import io.evcharge.station.api.dto.StationRequest;
import io.evcharge.station.api.dto.StationResponse;
import io.evcharge.station.domain.StationStatus;
import io.evcharge.station.service.StationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
@Tag(name = "Stations")
public class StationController {

    private final StationService stationService;

    @GetMapping
    public Page<StationResponse> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) StationStatus status,
            Pageable pageable) {
        return stationService.search(city, status, pageable);
    }

    @GetMapping("/{id}")
    public StationResponse get(@PathVariable Long id) { return stationService.get(id); }

    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationResponse create(@Valid @RequestBody StationRequest req,
                                  @RequestHeader("X-User-Id") Long userId) {
        return stationService.create(req, userId);
    }

    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    @PutMapping("/{id}")
    public StationResponse update(@PathVariable Long id, @Valid @RequestBody StationRequest req) {
        return stationService.update(id, req);
    }

    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        stationService.delete(id);
    }
}
