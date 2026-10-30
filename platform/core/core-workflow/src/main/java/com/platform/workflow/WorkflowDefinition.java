package com.platform.workflow;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workflow_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDefinition extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "entity_type", nullable = false)
    private String entityType; // e.g. TRANSFER, PROMOTION, LEAVE

    @Column(name = "steps_config", columnDefinition = "TEXT")
    private String stepsConfig; // JSON: [{level:1, approverRole:"HR_MANAGER"}, ...]

    @Column(name = "escalation_days", nullable = false)
    private int escalationDays = 3;
}
