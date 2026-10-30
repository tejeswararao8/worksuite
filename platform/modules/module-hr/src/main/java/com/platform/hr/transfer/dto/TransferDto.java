package com.platform.hr.transfer.dto;

import com.platform.hr.transfer.entity.Transfer.TransferStatus;
import com.platform.hr.transfer.entity.Transfer.TransferType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransferDto {

    @Data
    public static class CreateRequest {
        @NotNull private UUID employeeId;
        @NotNull private TransferType transferType;
        private UUID toDepartmentId;
        private UUID toBranchId;
        private UUID toManagerId;
        private UUID toTeamId;
        @NotNull private LocalDate effectiveDate;
        private String reason;
    }

    @Data
    public static class ApproveRequest {
        private String comments;
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
        private TransferType transferType;
        private UUID fromDepartmentId;
        private String fromDepartmentName;
        private UUID toDepartmentId;
        private String toDepartmentName;
        private UUID fromBranchId;
        private String fromBranchName;
        private UUID toBranchId;
        private String toBranchName;
        private UUID fromManagerId;
        private String fromManagerName;
        private UUID toManagerId;
        private String toManagerName;
        private UUID fromTeamId;
        private String fromTeamName;
        private UUID toTeamId;
        private String toTeamName;
        private LocalDate effectiveDate;
        private String reason;
        private TransferStatus status;
        private UUID approvedBy;
        private LocalDateTime approvedAt;
        private UUID workflowInstanceId;
    }
}
