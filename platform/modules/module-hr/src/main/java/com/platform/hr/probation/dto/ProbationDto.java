package com.platform.hr.probation.dto;

import com.platform.hr.probation.entity.Probation.ProbationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProbationDto {

    @Data
    public static class CreateRequest {
        @NotNull private UUID employeeId;
        @NotNull private LocalDate startDate;
        @NotNull private LocalDate endDate;
    }

    @Data
    public static class ConfirmRequest {
        private String remarks;
    }

    @Data
    public static class ExtendRequest {
        @NotNull private LocalDate newEndDate;
        @NotNull private String reason;
    }

    @Data
    public static class RejectRequest {
        @NotNull private String reason;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private String employeeName;
        private String employeeCode;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate extendedEndDate;
        private ProbationStatus status;
        private String remarks;
        private UUID approvedBy;
        private LocalDateTime approvedAt;
        private long daysRemaining;
    }
}
