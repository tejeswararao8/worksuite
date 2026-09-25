package com.hrms.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class OnboardingDto {

    @Data
    public static class InitiateRequest {
        @NotNull private UUID employeeId;
    }

    @Data
    public static class AddTaskRequest {
        @NotBlank private String taskName;
        private String description;
        private boolean mandatory;
        private Integer sequenceOrder;
    }

    @Data
    public static class TaskResponse {
        private UUID id;
        private String taskName;
        private String description;
        private boolean mandatory;
        private boolean completed;
        private UUID completedBy;
        private LocalDateTime completedAt;
        private Integer sequenceOrder;
    }
}
