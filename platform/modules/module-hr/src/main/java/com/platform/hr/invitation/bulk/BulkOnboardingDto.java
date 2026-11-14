package com.platform.hr.invitation.bulk;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class BulkOnboardingDto {

    @Getter
    @Builder
    public static class RowResult {
        private int rowNumber;
        private String email;
        private String employeeCode;
        private RowStatus status;
        private String errorField;
        private String errorMessage;
        private String invitationId;
    }

    @Getter
    @Builder
    public static class ImportResponse {
        private int totalRows;
        private int successCount;
        private int failedCount;
        private int invitationPendingCount;
        private int emailDeliveryFailureCount;
        private List<RowResult> results;
    }

    public enum RowStatus {
        SUCCESS,
        INVITATION_PENDING,
        FAILED,
        EMAIL_DELIVERY_FAILED
    }
}
