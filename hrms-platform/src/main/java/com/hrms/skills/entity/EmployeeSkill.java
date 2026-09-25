package com.hrms.skills.entity;

import com.hrms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSkill extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type", nullable = false)
    private SkillType skillType;

    @Column(name = "proficiency_level")
    private String proficiencyLevel;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    public enum SkillType { PRIMARY, SECONDARY }
}
