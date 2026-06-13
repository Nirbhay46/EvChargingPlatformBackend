package io.evcharge.notification.api;

import io.evcharge.notification.api.dto.NotificationResponseDto;
import io.evcharge.notification.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public Page<NotificationResponseDto> myNotifications(@RequestHeader("X-User-Email") String email, Pageable pg) {
        return notificationQueryService.listForRecipient(email, pg);
    }
}
