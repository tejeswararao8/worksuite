package com.platform.hr.department.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DepartmentDto {

    @Data
    public static class CreateRequest {
        @NotBlank private String name;
        @NotBlank private String code;
        private String description;
        private UUID parentDepartmentId;
        private UUID headEmployeeId;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String description;
        private UUID parentDepartmentId;
        private UUID headEmployeeId;
    }

    @Data
    public static class Response {
        private UUID id;
        private String name;
        private String code;
        private String description;
        private UUID parentDepartmentId;
        private String parentDepartmentName;
        private UUID headEmployeeId;
        private String headEmployeeName;
        private long employeeCount;
        private long teamCount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class Summary {
        private UUID id;
        private String name;
        private String code;
        private UUID parentDepartmentId;
    }

    @Data
    public static class OrgNode {
        private UUID id;
        private String name;
        private String code;
        private String headEmployeeName;
        private long employeeCount;
        private List<OrgNode> children;
    }
}
