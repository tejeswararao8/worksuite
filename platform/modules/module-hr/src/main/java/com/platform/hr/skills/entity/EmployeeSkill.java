package com.platform.hr.skills.entity;

import com.platform.tenancy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "employee_skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeSkill extends BaseEntity {
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "skill_name", nullable = false) private String skillName;
    @Column(name = "skill_type") private String skillType;
    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency") private Proficiency proficiency;
    @Column(name = "years_of_experience") private Integer yearsOfExperience;

    public enum Proficiency { BEGINNER, INTERMEDIATE, ADVANCED, EXPERT }
}
