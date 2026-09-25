package com.hrms.team.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "lead_employee_id")
    private UUID leadEmployeeId;
}
