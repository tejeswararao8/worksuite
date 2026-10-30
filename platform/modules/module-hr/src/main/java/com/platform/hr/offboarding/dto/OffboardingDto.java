package com.platform.hr.offboarding.dto;

import com.platform.hr.offboarding.entity.OffboardingChecklist.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class OffboardingDto {

    @Data
    public static class InitiateRequest {
        @NotNull private UUID employeeId;
        @NotNull private LocalDate lastWorkingDay;
        private String resignationReason;
        private OffboardingType type = OffboardingType.RESIGNATION;
    }

    @Data
    public static class CreateTaskRequest {
        private String taskName;
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
        private LocalDate lastWorkingDay;
        private int totalTasks;
        private int completedTasks;
        private int pendingTasks;
        private double completionPercentage;
        private boolean fullyCompleted;
    }

    public enum OffboardingType { RESIGNATION, TERMINATION, RETIREMENT, CONTRACT_END }
}
