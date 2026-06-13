package io.evcharge.notification.converter;

import io.evcharge.notification.api.dto.NotificationResponseDto;
import io.evcharge.notification.domain.NotificationLog;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogToNotificationResponseDtoConverter implements Converter<NotificationLog, NotificationResponseDto> {

    @Override
    public NotificationResponseDto convert(NotificationLog n) {
        return new NotificationResponseDto(
                n.getId(), n.getChannel(), n.getRecipient(), n.getTemplateId(),
                n.getSubject(), n.getBody(), n.getStatus(),
                n.getProviderMessageId(), n.getErrorMessage(), n.getCreatedAt()
        );
    }
}
