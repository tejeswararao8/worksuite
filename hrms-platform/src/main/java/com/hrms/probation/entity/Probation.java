package com.hrms.probation.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "probations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Probation extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "extended_end_date")
    private LocalDate extendedEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProbationStatus status;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public enum ProbationStatus {
        IN_PROGRESS, CONFIRMED, EXTENDED, REJECTED
    }
}
