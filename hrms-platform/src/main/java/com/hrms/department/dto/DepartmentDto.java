package com.hrms.department.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class DepartmentDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;
        @NotBlank
        private String code;
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
        private UUID headEmployeeId;
        private LocalDateTime createdAt;
    }
}
