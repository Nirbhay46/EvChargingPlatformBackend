package io.evcharge.notification.repo;

import io.evcharge.notification.domain.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Page<NotificationLog> findByRecipientOrderByCreatedAtDesc(String recipient, Pageable pg);
}
