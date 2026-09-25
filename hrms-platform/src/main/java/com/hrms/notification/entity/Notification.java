package com.hrms.notification.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "is_read")
    private boolean read;

    public enum NotificationType {
        DOCUMENT_EXPIRY, ONBOARDING, OFFBOARDING, PROBATION,
        TRANSFER, PROMOTION, ASSET, GENERAL
    }
}
