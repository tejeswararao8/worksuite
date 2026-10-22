package com.platform.hr.onboarding.entity;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "onboarding_checklists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OnboardingChecklist extends BaseEntity {
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "task_name", nullable = false) private String taskName;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) private TaskStatus status = TaskStatus.PENDING;
    @Column(name = "assigned_to") private UUID assignedTo;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(name = "completed_at") private LocalDate completedAt;

    public enum TaskStatus { PENDING, IN_PROGRESS, COMPLETED }
}
