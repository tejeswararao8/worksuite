package com.hrms.notification.dto;

import com.hrms.notification.entity.Notification.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDto {

    @Data
    public static class Response {
        private UUID id;
        private String title;
        private String message;
        private NotificationType type;
        private String referenceId;
        private boolean read;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UnreadCount {
        private long count;

        public UnreadCount(long count) {
            this.count = count;
        }
    }
}
