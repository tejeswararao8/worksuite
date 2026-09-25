package com.hrms.onboarding.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "onboarding_checklists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OnboardingChecklist extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_mandatory")
    private boolean mandatory;

    @Column(name = "is_completed")
    private boolean completed;

    @Column(name = "completed_by")
    private UUID completedBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;
}
