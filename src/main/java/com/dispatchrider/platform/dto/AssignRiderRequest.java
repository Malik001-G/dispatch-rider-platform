package com.dispatchrider.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * FR-9: admin manually assigns a rider.
 * FR-7a: admin may override the system-suggested nearest-neighbor sequence before confirming -
 * pass the desired stop id order here; if null, the auto-suggested sequence is used as-is.
 */
@Data
public class AssignRiderRequest {
    @NotNull private Long riderUserId;
    private List<Long> stopIdSequenceOverride;
}
