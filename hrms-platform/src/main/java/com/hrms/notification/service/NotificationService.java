package com.hrms.notification.service;

import com.hrms.common.dto.PagedResponse;
import com.hrms.common.util.SecurityUtils;
import com.hrms.notification.dto.NotificationDto;
import com.hrms.notification.entity.Notification;
import com.hrms.notification.entity.Notification.NotificationType;
import com.hrms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void send(UUID companyId, UUID recipientUserId, String title,
                     String message, NotificationType type, String referenceId, String referenceType) {
        Notification notification = Notification.builder()
                .companyId(companyId)
                .recipientUserId(recipientUserId)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationDto.Response> getMyNotifications(Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return new PagedResponse<>(
                notificationRepository.findByRecipientUserIdAndCompanyIdOrderByCreatedAtDesc(userId, companyId, pageable)
                        .map(this::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public NotificationDto.UnreadCount getUnreadCount() {
        UUID userId = SecurityUtils.getCurrentUserId();
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        return new NotificationDto.UnreadCount(
                notificationRepository.countByRecipientUserIdAndCompanyIdAndReadFalse(userId, companyId));
    }

    public void markAllAsRead() {
        UUID userId = SecurityUtils.getCurrentUserId();
        UUID companyId = SecurityUtils.getCurrentCompanyId();
        notificationRepository.markAllAsRead(userId, companyId);
    }

    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    private NotificationDto.Response toResponse(Notification n) {
        NotificationDto.Response r = new NotificationDto.Response();
        r.setId(n.getId());
        r.setTitle(n.getTitle());
        r.setMessage(n.getMessage());
        r.setType(n.getType());
        r.setReferenceId(n.getReferenceId());
        r.setRead(n.isRead());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
