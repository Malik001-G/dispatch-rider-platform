package com.dispatchrider.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** FR-9a: rider manually toggles ACTIVE <-> OFFLINE (BUSY is system-controlled, not settable here). */
@Data
public class RiderStatusUpdateRequest {
    @NotNull private com.dispatchrider.platform.entity.RiderStatus status;
}
