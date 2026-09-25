package com.hrms.promotion.dto;

import com.hrms.promotion.entity.Promotion.PromotionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PromotionDto {

    @Data
    public static class InitiateRequest {
        @NotNull private UUID employeeId;
        @NotNull private UUID toDesignationId;
        @NotNull private LocalDate effectiveDate;
        private String reason;
    }

    @Data
    public static class ActionRequest {
        private String remarks;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID employeeId;
        private UUID fromDesignationId;
        private UUID toDesignationId;
        private LocalDate effectiveDate;
        private String reason;
        private PromotionStatus status;
        private UUID approvedBy;
        private LocalDateTime approvedAt;
    }
}
