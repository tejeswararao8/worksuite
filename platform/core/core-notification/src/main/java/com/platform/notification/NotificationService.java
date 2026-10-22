package com.platform.notification;

import com.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Async
    public void send(NotificationRequest request) {
        // Save in-app notification
        Notification notification = Notification.builder()
            .recipientId(request.getRecipientId())
            .title(request.getTitle())
            .message(request.getMessage())
            .type(request.getType())
            .channel(Notification.NotificationChannel.IN_APP)
            .referenceId(request.getReferenceId())
            .referenceType(request.getReferenceType())
            .isRead(false)
            .build();
        notification.setCompanyId(request.getCompanyId());
        notificationRepository.save(notification);

        // Send email if requested
        if (request.isSendEmail() && request.getRecipientEmail() != null) {
            sendEmail(request.getRecipientEmail(), request.getTitle(), request.getMessage());
        }
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Email send failed to {}: {}", to, e.getMessage());
        }
    }

    public Page<Notification> getForCurrentUser(UUID userId, Pageable pageable) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return notificationRepository.findByRecipientIdAndCompanyIdOrderByCreatedAtDesc(userId, companyId, pageable);
    }

    public long countUnread(UUID userId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        return notificationRepository.countByRecipientIdAndCompanyIdAndIsReadFalse(userId, companyId);
    }

    @Transactional
    public void markRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllRead(UUID userId) {
        UUID companyId = UUID.fromString(TenantContext.getTenantId());
        notificationRepository.markAllReadByRecipient(userId, companyId);
    }
}
