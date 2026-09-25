package com.hrms.designation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class DesignationDto {

    @Data
    public static class Request {
        @NotBlank
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
        private LocalDateTime createdAt;
    }
}
