package com.platform.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientIdAndCompanyIdOrderByCreatedAtDesc(UUID recipientId, UUID companyId, Pageable pageable);
    long countByRecipientIdAndCompanyIdAndIsReadFalse(UUID recipientId, UUID companyId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId AND n.companyId = :companyId")
    void markAllReadByRecipient(UUID recipientId, UUID companyId);
}
