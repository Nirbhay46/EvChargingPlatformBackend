package io.evcharge.notification.kafka;

import io.evcharge.common.events.NotificationEvent;
import io.evcharge.common.events.Topics;
import io.evcharge.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = Topics.NOTIFICATION_EVENTS, groupId = "notification-service",
            containerFactory = "notificationEventListenerFactory")
    public void onNotification(NotificationEvent ev) {
        log.info("Received Notification template={} to={}", ev.templateId(), ev.recipient());
        if ("EMAIL".equalsIgnoreCase(ev.channel())) {
            emailService.send(ev);
        }
    }
}
