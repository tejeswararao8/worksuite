package com.platform.hr.designation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class DesignationDto {

    @Data
    public static class CreateRequest {
        @NotBlank private String title;
        private String description;
        private Integer level;
        private String grade;
    }

    @Data
    public static class UpdateRequest {
        private String title;
        private String description;
        private Integer level;
        private String grade;
    }

    @Data
    public static class Response {
        private UUID id;
        private String title;
        private String description;
        private Integer level;
        private String grade;
        private long employeeCount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class Summary {
        private UUID id;
        private String title;
        private Integer level;
        private String grade;
    }
}
