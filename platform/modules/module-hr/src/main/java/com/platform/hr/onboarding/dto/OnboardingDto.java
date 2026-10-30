package com.platform.hr.onboarding.dto;

import com.platform.hr.onboarding.entity.OnboardingChecklist.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class OnboardingDto {

    @Data
    public static class CreateTaskRequest {
        @NotBlank private String taskName;
        private String description;
        private UUID assignedTo;
        private LocalDate dueDate;
    }

    @Data
    public static class UpdateTaskRequest {
        private String taskName;
        private String description;
        private UUID assignedTo;
        private LocalDate dueDate;
        private TaskStatus status;
    }

    @Data
    public static class CompleteTaskRequest {
        private String completionNote;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private String employeeName;
        private String taskName;
        private String description;
        private TaskStatus status;
        private UUID assignedTo;
        private String assignedToName;
        private LocalDate dueDate;
        private LocalDate completedAt;
        private boolean overdue;
    }

    @Data
    public static class ProgressResponse {
        private UUID employeeId;
        private String employeeName;
        private int totalTasks;
        private int completedTasks;
        private int pendingTasks;
        private int overdueTasks;
        private double completionPercentage;
    }
}
