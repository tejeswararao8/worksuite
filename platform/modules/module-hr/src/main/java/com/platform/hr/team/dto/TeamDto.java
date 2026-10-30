package com.platform.hr.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TeamDto {

    @Data
    public static class CreateRequest {
        @NotBlank private String name;
        private String description;
        @NotNull private UUID departmentId;
        private UUID leadEmployeeId;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String description;
        private UUID departmentId;
        private UUID leadEmployeeId;
    }

    @Data
    public static class Response {
        private UUID id;
        private String name;
        private String description;
        private UUID departmentId;
        private String departmentName;
        private UUID leadEmployeeId;
        private String leadEmployeeName;
        private long memberCount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class Summary {
        private UUID id;
        private String name;
        private UUID departmentId;
    }

    @Data
    public static class MemberRequest {
        @NotNull private UUID employeeId;
    }

    @Data
    public static class MemberResponse {
        private UUID employeeId;
        private String employeeCode;
        private String firstName;
        private String lastName;
        private String designation;
    }
}
