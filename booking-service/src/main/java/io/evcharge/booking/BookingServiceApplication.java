package io.evcharge.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;

@EnableJpaAuditing
@EnableFeignClients
@EnableKafka
@EnableRetry
@SpringBootApplication
public class BookingServiceApplication {
    public static void main(String[] args) { SpringApplication.run(BookingServiceApplication.class, args); }
}
