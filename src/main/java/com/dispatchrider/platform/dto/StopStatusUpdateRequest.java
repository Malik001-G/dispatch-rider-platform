package com.dispatchrider.platform.dto;

import com.dispatchrider.platform.entity.StopStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * FR-11: rider updates a stop's status.
 * When moving to DELIVERED, otp must match the stop's generated OTP (FR-13).
 */
@Data
public class StopStatusUpdateRequest {
    @NotNull private StopStatus newStatus;
    private String otp;
}
