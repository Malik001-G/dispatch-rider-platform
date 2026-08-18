package com.dispatchrider.platform.entity;

/**
 * FR-7d and FR-7b-i order lifecycle.
 * PENDING_PAYMENT -> PAID (webhook) -> ASSIGNED -> IN_PROGRESS -> COMPLETED
 *                                   \-> CANCELLED_NO_RIDER (auto, FR-7b-i)
 * PENDING_PAYMENT -> CANCELLED (FR-7g, sender cancels pre-assignment)
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    CANCELLED_NO_RIDER
}
