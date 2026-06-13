package io.evcharge.notification.service;

import io.evcharge.common.events.NotificationEvent;
import io.evcharge.notification.domain.NotificationLog;
import io.evcharge.notification.repo.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock SesClient ses;
    @Mock NotificationLogRepository repo;
    @InjectMocks EmailService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "fromEmail", "no-reply@evcharge.io");
    }

    @Test
    void send_storesSentLog_onSuccess() {
        when(ses.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("ses-123").build());

        var ev = new NotificationEvent("EMAIL", "u@x.io", "BOOKING_CONFIRMED",
                "Booking confirmed", Map.of("fullName", "U", "stationName", "Hub",
                "startTime", "2026-01-01T10:00", "endTime", "2026-01-01T11:00",
                "amount", "10.00", "currency", "USD", "chargeId", "ch_x", "reason", ""));

        service.send(ev);

        ArgumentCaptor<NotificationLog> cap = ArgumentCaptor.forClass(NotificationLog.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("SENT");
        assertThat(cap.getValue().getProviderMessageId()).isEqualTo("ses-123");
        assertThat(cap.getValue().getBody()).contains("Hub");
    }

    @Test
    void send_storesFailedLog_andRethrows_onSesException() {
        SesException ex = (SesException) SesException.builder().message("boom").build();
        when(ses.sendEmail(any(SendEmailRequest.class))).thenThrow(ex);

        var ev = new NotificationEvent("EMAIL", "u@x.io", "BOOKING_CONFIRMED",
                "Booking confirmed", Map.of());

        assertThatThrownBy(() -> service.send(ev)).isInstanceOf(SesException.class);

        ArgumentCaptor<NotificationLog> cap = ArgumentCaptor.forClass(NotificationLog.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("FAILED");
        assertThat(cap.getValue().getErrorMessage()).contains("boom");
    }
}
