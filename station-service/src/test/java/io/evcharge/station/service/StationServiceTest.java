package io.evcharge.station.service;

import io.evcharge.station.api.dto.StationRequest;
import io.evcharge.station.domain.ConnectorType;
import io.evcharge.station.domain.Station;
import io.evcharge.station.domain.StationStatus;
import io.evcharge.station.exception.ApiException;
import io.evcharge.station.repo.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock StationRepository repo;
    @InjectMocks StationService service;

    @Test
    void create_persistsAndReturnsActive() {
        var req = new StationRequest("Hub", "Seattle", "1st Ave", 47.6, -122.3,
                4, 150, ConnectorType.CCS, new BigDecimal("0.30"), "USD", null);
        when(repo.save(any(Station.class))).thenAnswer(inv -> {
            Station s = inv.getArgument(0); s.setId(1L); return s;
        });

        var res = service.create(req, 99L);

        assertThat(res.id()).isEqualTo(1L);
        assertThat(res.status()).isEqualTo(StationStatus.ACTIVE);
        assertThat(res.currency()).isEqualTo("USD");
    }

    @Test
    void get_throwsNotFound_whenMissing() {
        when(repo.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(5L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not found");
    }
}
