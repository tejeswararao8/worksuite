package com.hrms.skills.dto;

import com.hrms.skills.entity.EmployeeSkill.SkillType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

public class SkillsDto {

    @Data
    public static class AddSkillRequest {
        @NotBlank private String skillName;
        @NotNull  private SkillType skillType;
        private String proficiencyLevel;
        private Integer yearsOfExperience;
    }

    @Data
    public static class SkillResponse {
        private UUID id;
        private String skillName;
        private SkillType skillType;
        private String proficiencyLevel;
        private Integer yearsOfExperience;
    }

    @Data
    public static class AddCertificationRequest {
        @NotBlank private String certificationName;
        private String issuingOrganization;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String credentialId;
        private String credentialUrl;
    }

    @Data
    public static class CertificationResponse {
        private UUID id;
        private String certificationName;
        private String issuingOrganization;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String credentialId;
        private String credentialUrl;
    }
}
