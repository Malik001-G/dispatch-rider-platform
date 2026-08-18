package com.dispatchrider.platform.entity;

/**
 * FR-9a: ACTIVE/OFFLINE toggled manually by the rider.
 * BUSY is set automatically by the system on assignment and cleared automatically
 * once all stops on the rider's current order are delivered or failed.
 */
public enum RiderStatus {
    ACTIVE,
    BUSY,
    OFFLINE
}
