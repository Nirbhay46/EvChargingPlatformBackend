package io.evcharge.notification.service;

import io.evcharge.common.events.NotificationEvent;
import io.evcharge.notification.domain.NotificationLog;
import io.evcharge.notification.repo.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SesClient ses;
    private final NotificationLogRepository repo;

    @Value("${notification.from-email:no-reply@evcharge.io}") String fromEmail;

    @Retryable(retryFor = SesException.class, maxAttempts = 3,
               backoff = @Backoff(delay = 500, multiplier = 2))
    public void send(NotificationEvent ev) {
        String template = TemplateRenderer.TEMPLATES.getOrDefault(ev.templateId(),
                "{{subject}}\n\nNo template registered for " + ev.templateId());
        String body = TemplateRenderer.render(template, ev.data());

        SendEmailRequest req = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder().toAddresses(ev.recipient()).build())
                .message(Message.builder()
                        .subject(Content.builder().data(ev.subject()).charset("UTF-8").build())
                        .body(Body.builder()
                                .text(Content.builder().data(body).charset("UTF-8").build())
                                .build())
                        .build())
                .build();

        NotificationLog rec = NotificationLog.builder()
                .channel(ev.channel()).recipient(ev.recipient())
                .templateId(ev.templateId()).subject(ev.subject()).body(body)
                .build();

        try {
            SendEmailResponse resp = ses.sendEmail(req);
            rec.setStatus("SENT");
            rec.setProviderMessageId(resp.messageId());
            log.info("Email sent to {} via SES, messageId={}", ev.recipient(), resp.messageId());
        } catch (Exception ex) {
            rec.setStatus("FAILED");
            rec.setErrorMessage(ex.getMessage());
            log.error("Failed to send email to {}: {}", ev.recipient(), ex.getMessage());
            throw ex; // trigger retry
        } finally {
            repo.save(rec);
        }
    }
}
