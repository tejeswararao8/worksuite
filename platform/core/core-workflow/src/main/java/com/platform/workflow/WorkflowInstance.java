package com.platform.workflow;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "workflow_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private WorkflowDefinition definition;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkflowStatus status = WorkflowStatus.PENDING;

    @Column(name = "current_step", nullable = false)
    private int currentStep = 1;

    @Column(name = "initiated_by", nullable = false)
    private UUID initiatedBy;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    public enum WorkflowStatus {
        PENDING, IN_PROGRESS, APPROVED, REJECTED, ESCALATED, CANCELLED
    }
}
