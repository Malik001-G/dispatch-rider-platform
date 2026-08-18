package com.dispatchrider.platform.entity;

/** FR-11: per-stop status, independently trackable. */
public enum StopStatus {
    PENDING,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    FAILED
}
