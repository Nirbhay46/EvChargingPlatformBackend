package io.evcharge.booking;

import io.evcharge.booking.client.StationClient;
import io.evcharge.booking.kafka.BookingEventProducer;
import io.evcharge.booking.repo.BookingRepository;
import io.evcharge.common.events.BookingCreatedEvent;
import io.evcharge.common.events.Topics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration test: publishes a BookingCreatedEvent through the real
 * producer wired in Spring, then consumes from a Testcontainers Kafka
 * to verify end-to-end serialization & broker delivery.
 */
@Testcontainers
@SpringBootTest
class BookingKafkaIntegrationTest {

    @Container
    static MySQLContainer<?> pg = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("bookingdb").withUsername("ev").withPassword("ev");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
        r.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        r.add("clients.station-service.url", () -> "http://localhost:1");
    }

    @Autowired BookingEventProducer producer;

    @Test
    void publishes_to_booking_events_topic() {
        BookingCreatedEvent ev = new BookingCreatedEvent(1L, 1L, "u@x.io",
                10L, "Hub", LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                new BigDecimal("12.34"));
        producer.publishBookingCreated(ev);

        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "verify-" + UUID.randomUUID());
        cfg.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> c = new KafkaConsumer<>(cfg)) {
            c.subscribe(List.of(Topics.BOOKING_EVENTS));
            await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
                var records = c.poll(Duration.ofMillis(500));
                assertThat(records.iterator().hasNext()).isTrue();
                ConsumerRecord<String, String> rec = records.iterator().next();
                assertThat(rec.value()).contains("\"bookingId\":1");
                assertThat(rec.value()).contains("\"userEmail\":\"u@x.io\"");
            });
        }
    }

    @TestConfiguration
    static class MockStationClient {
        @Bean @Primary
        StationClient stationClient() { return id -> null; }
    }
}
