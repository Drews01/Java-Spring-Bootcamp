package com.example.demo.enums;

public enum LoanStatus {
    SUBMITTED,
    IN_REVIEW,
    WAITING_APPROVAL,
    APPROVED_WAITING_DISBURSEMENT,
    DISBURSED,
    REJECTED;

    public static boolean isMarketingQueue(String status) {
        return SUBMITTED.name().equals(status) || IN_REVIEW.name().equals(status);
    }

    public static boolean isBranchManagerQueue(String status) {
        return WAITING_APPROVAL.name().equals(status);
    }

    public static boolean isBackOfficeQueue(String status) {
        return APPROVED_WAITING_DISBURSEMENT.name().equals(status);
    }
}
