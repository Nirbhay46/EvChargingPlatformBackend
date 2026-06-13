package io.evcharge.notification.config;

import io.evcharge.common.events.NotificationEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}") String bootstrap;

    @Bean
    public ConsumerFactory<String, NotificationEvent> notificationEventConsumerFactory() {
        JsonDeserializer<NotificationEvent> deser = new JsonDeserializer<>(NotificationEvent.class, false);
        deser.addTrustedPackages("io.evcharge.*");
        Map<String, Object> p = new HashMap<>();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(p, new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deser));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> notificationEventListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(notificationEventConsumerFactory());
        return f;
    }
}
