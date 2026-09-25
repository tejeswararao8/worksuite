package com.hrms.employee.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "alternate_mobile")
    private String alternateMobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nationality")
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatus maritalStatus;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "current_address", columnDefinition = "TEXT")
    private String currentAddress;

    @Column(name = "permanent_address", columnDefinition = "TEXT")
    private String permanentAddress;

    // Employment Info
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "designation_id")
    private UUID designationId;

    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "work_location")
    private String workLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false)
    private EmploymentStatus employmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    public enum Gender { MALE, FEMALE, OTHER }

    public enum MaritalStatus { SINGLE, MARRIED, DIVORCED, WIDOWED }

    public enum EmploymentStatus {
        ACTIVE, PROBATION, ON_LEAVE, RESIGNED, TERMINATED, RETIRED
    }

    public enum EmploymentType {
        FULL_TIME, PART_TIME, CONTRACT, INTERN
    }
}
