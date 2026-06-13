package io.evcharge.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Audited
@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)   private String channel;
    @Column(nullable = false, length = 120)  private String recipient;
    @Column(name = "template_id", nullable = false, length = 64) private String templateId;
    @Column(nullable = false, length = 200)  private String subject;
    @Column(columnDefinition = "text")       private String body;

    @Column(nullable = false, length = 32)   private String status;   // SENT | FAILED
    @Column(name = "provider_message_id", length = 100) private String providerMessageId;
    @Column(name = "error_message", length = 500)       private String errorMessage;

    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
