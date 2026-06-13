package io.evcharge.notification.service;

import io.evcharge.notification.api.dto.NotificationResponseDto;
import io.evcharge.notification.converter.NotificationLogToNotificationResponseDtoConverter;
import io.evcharge.notification.repo.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationLogRepository repo;
    private final NotificationLogToNotificationResponseDtoConverter converter;

    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> listForRecipient(String email, Pageable pg) {
        return repo.findByRecipientOrderByCreatedAtDesc(email, pg).map(converter::convert);
    }
}
