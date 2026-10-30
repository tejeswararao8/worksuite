package com.platform.workflow;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    private WorkflowInstance instance;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(name = "approver_id")
    private UUID approverId;

    @Column(name = "approver_role")
    private String approverRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private StepAction action;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "acted_at")
    private LocalDateTime actedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    public enum StepAction {
        PENDING, APPROVED, REJECTED, ESCALATED
    }
}
