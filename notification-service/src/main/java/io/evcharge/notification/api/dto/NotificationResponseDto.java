package io.evcharge.notification.api.dto;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        String channel,
        String recipient,
        String templateId,
        String subject,
        String body,
        String status,
        String providerMessageId,
        String errorMessage,
        LocalDateTime createdAt
) {}
