package io.evcharge.notification.service;

import io.evcharge.notification.domain.NotificationLog;
import io.evcharge.notification.repo.NotificationLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @Mock NotificationLogRepository repo;
    @InjectMocks NotificationQueryService service;

    @Test
    void listForRecipient_returnsPagedLogs() {
        NotificationLog n = NotificationLog.builder().id(1L)
                .channel("EMAIL").recipient("u@x.io").templateId("BOOKING_CONFIRMED")
                .subject("ok").status("SENT").build();
        when(repo.findByRecipientOrderByCreatedAtDesc(eq("u@x.io"), any()))
                .thenReturn(new PageImpl<>(List.of(n)));

        var res = service.listForRecipient("u@x.io", PageRequest.of(0, 10));
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getTemplateId()).isEqualTo("BOOKING_CONFIRMED");
    }
}
