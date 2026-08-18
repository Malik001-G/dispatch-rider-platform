package com.dispatchrider.platform.entity;

/** FR-2: riders are manually reviewed before they can go ACTIVE. */
public enum RiderApprovalStatus {
    PENDING_REVIEW,
    APPROVED,
    REJECTED
}
