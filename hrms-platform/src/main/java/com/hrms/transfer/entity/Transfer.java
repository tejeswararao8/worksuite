package com.hrms.transfer.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false)
    private TransferType transferType;

    @Column(name = "from_department_id")
    private UUID fromDepartmentId;

    @Column(name = "to_department_id")
    private UUID toDepartmentId;

    @Column(name = "from_branch_id")
    private UUID fromBranchId;

    @Column(name = "to_branch_id")
    private UUID toBranchId;

    @Column(name = "from_manager_id")
    private UUID fromManagerId;

    @Column(name = "to_manager_id")
    private UUID toManagerId;

    @Column(name = "from_team_id")
    private UUID fromTeamId;

    @Column(name = "to_team_id")
    private UUID toTeamId;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public enum TransferType {
        DEPARTMENT, BRANCH, MANAGER, TEAM
    }

    public enum TransferStatus {
        PENDING, APPROVED, REJECTED, COMPLETED
    }
}
