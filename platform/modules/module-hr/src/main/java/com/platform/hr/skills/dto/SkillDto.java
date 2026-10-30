package com.platform.hr.skills.dto;

import com.platform.hr.skills.entity.EmployeeSkill.Proficiency;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

public class SkillDto {

    @Data
    public static class CreateSkillRequest {
        @NotBlank private String skillName;
        private String skillType;
        private Proficiency proficiency;
        private Integer yearsOfExperience;
    }

    @Data
    public static class UpdateSkillRequest {
        private String skillName;
        private String skillType;
        private Proficiency proficiency;
        private Integer yearsOfExperience;
    }

    @Data
    public static class SkillResponse {
        private UUID id;
        private UUID employeeId;
        private String skillName;
        private String skillType;
        private Proficiency proficiency;
        private Integer yearsOfExperience;
    }

    @Data
    public static class CreateCertificationRequest {
        @NotBlank private String name;
        private String issuingOrg;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String credentialUrl;
    }

    @Data
    public static class UpdateCertificationRequest {
        private String name;
        private String issuingOrg;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String credentialUrl;
    }

    @Data
    public static class CertificationResponse {
        private UUID id;
        private UUID employeeId;
        private String name;
        private String issuingOrg;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String credentialUrl;
        private boolean expired;
        private boolean expiringSoon;
    }
}
