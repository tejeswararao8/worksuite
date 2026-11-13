package com.platform.tenancy;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tenant registry — one record per company using the platform.
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "logo_path")
    private String logoPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TenantStatus status = TenantStatus.PENDING_REVIEW;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @PrePersist
    protected void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public enum TenantStatus {
        PENDING_REVIEW, ACTIVE, SUSPENDED, REJECTED, TRIAL, CANCELLED;

        /** Returns true if a transition from this status to {@code next} is valid. */
        public boolean canTransitionTo(TenantStatus next) {
            return switch (this) {
                case PENDING_REVIEW -> next == ACTIVE || next == REJECTED;
                case ACTIVE         -> next == SUSPENDED;
                case SUSPENDED      -> next == ACTIVE;
                case REJECTED, TRIAL, CANCELLED -> false;
            };
        }
    }
}
