package com.hrms.probation.dto;

import com.hrms.probation.entity.Probation.ProbationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProbationDto {

    @Data
    public static class InitiateRequest {
        @NotNull private UUID employeeId;
        @NotNull private LocalDate startDate;
        @NotNull private LocalDate endDate;
        private String remarks;
    }

    @Data
    public static class ExtendRequest {
        @NotNull private LocalDate newEndDate;
        private String remarks;
    }

    @Data
    public static class ActionRequest {
        private String remarks;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate extendedEndDate;
        private ProbationStatus status;
        private String remarks;
        private UUID approvedBy;
        private LocalDateTime approvedAt;
    }
}
