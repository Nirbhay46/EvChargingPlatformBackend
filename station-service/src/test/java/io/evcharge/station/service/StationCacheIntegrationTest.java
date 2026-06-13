package io.evcharge.station.service;

import io.evcharge.station.api.dto.StationRequest;
import io.evcharge.station.domain.ConnectorType;
import io.evcharge.station.repo.StationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@code @Cacheable} on StationService.get() actually populates
 * Redis and serves subsequent reads without hitting the repository.
 */
@Testcontainers
@SpringBootTest
class StationCacheIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("stationdb").withUsername("ev").withPassword("ev");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.4-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired StationService stationService;
    @Autowired StationRepository stationRepository;
    @Autowired CacheManager cacheManager;

    @AfterEach
    void cleanup() {
        cacheManager.getCache("stations").clear();
    }

    @Test
    void get_populatesCache_andServesSubsequentReadFromCache() {
        var created = stationService.create(new StationRequest(
                "Cache Hub", "Tacoma", "1 St", 47.0, -122.0, 4, 150,
                ConnectorType.CCS, new BigDecimal("0.30"), "USD", null), 1L);

        // 1st call → populates cache
        var first = stationService.get(created.id());
        assertThat(cacheManager.getCache("stations").get(created.id())).isNotNull();

        // Delete row from DB directly (bypasses cache-evict)
        stationRepository.deleteById(created.id());

        // 2nd call should STILL succeed because cache holds the value
        var second = stationService.get(created.id());
        assertThat(second.id()).isEqualTo(first.id());
    }
}
