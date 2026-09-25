package com.hrms.offboarding.dto;

import com.hrms.offboarding.entity.OffboardingChecklist.OffboardingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class OffboardingDto {

    @Data
    public static class InitiateRequest {
        @NotNull private UUID employeeId;
        @NotNull private LocalDate lastWorkingDate;
        private String exitReason;
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
        private LocalDate lastWorkingDate;
        private String exitReason;
        private OffboardingStatus offboardingStatus;
    }
}
