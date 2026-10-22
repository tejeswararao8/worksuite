package com.platform.hr.skills.entity;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_certifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeCertification extends BaseEntity {
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "name", nullable = false) private String name;
    @Column(name = "issuing_org") private String issuingOrg;
    @Column(name = "issue_date") private LocalDate issueDate;
    @Column(name = "expiry_date") private LocalDate expiryDate;
    @Column(name = "credential_url") private String credentialUrl;
}
