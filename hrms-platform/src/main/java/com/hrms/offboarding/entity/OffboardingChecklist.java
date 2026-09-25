package com.hrms.offboarding.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offboarding_checklists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OffboardingChecklist extends BaseEntity {

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

    @Column(name = "last_working_date")
    private LocalDate lastWorkingDate;

    @Column(name = "exit_reason", columnDefinition = "TEXT")
    private String exitReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "offboarding_status")
    private OffboardingStatus offboardingStatus;

    public enum OffboardingStatus { IN_PROGRESS, COMPLETED }
}
