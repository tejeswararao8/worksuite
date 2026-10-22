package com.platform.notification;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class NotificationRequest {
    private UUID recipientId;
    private UUID companyId;
    private String title;
    private String message;
    private Notification.NotificationType type;
    private String referenceId;
    private String referenceType;
    private boolean sendEmail;
    private String recipientEmail;
}
