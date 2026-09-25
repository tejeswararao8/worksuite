package com.hrms.employee.dto;

import com.hrms.employee.entity.Employee.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class EmployeeDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        @Email
        @NotBlank
        private String email;
        private String mobile;
        private String alternateMobile;
        private Gender gender;
        private LocalDate dateOfBirth;
        private String nationality;
        private MaritalStatus maritalStatus;
        private String bloodGroup;
        @NotNull
        private LocalDate joiningDate;
        private UUID branchId;
        @NotNull
        private UUID departmentId;
        private UUID teamId;
        @NotNull
        private UUID designationId;
        private UUID managerId;
        private String workLocation;
        private EmploymentType employmentType;
        private String currentAddress;
        private String permanentAddress;
    }

    @Data
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String mobile;
        private String alternateMobile;
        private Gender gender;
        private LocalDate dateOfBirth;
        private String nationality;
        private MaritalStatus maritalStatus;
        private String bloodGroup;
        private String currentAddress;
        private String permanentAddress;
        private String workLocation;
    }

    @Data
    public static class Response {
        private UUID id;
        private String employeeCode;
        private String firstName;
        private String lastName;
        private String email;
        private String mobile;
        private Gender gender;
        private LocalDate dateOfBirth;
        private String nationality;
        private MaritalStatus maritalStatus;
        private String bloodGroup;
        private String photoUrl;
        private LocalDate joiningDate;
        private UUID branchId;
        private UUID departmentId;
        private UUID teamId;
        private UUID designationId;
        private UUID managerId;
        private String workLocation;
        private EmploymentStatus employmentStatus;
        private EmploymentType employmentType;
        private LocalDateTime createdAt;
    }

    @Data
    public static class Summary {
        private UUID id;
        private String employeeCode;
        private String firstName;
        private String lastName;
        private String email;
        private EmploymentStatus employmentStatus;
    }
}
