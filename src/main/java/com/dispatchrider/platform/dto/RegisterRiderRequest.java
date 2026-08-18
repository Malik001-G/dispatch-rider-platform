package com.dispatchrider.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** FR-2 - vehicle type is not collected: bicycle-only for Tier 1, so there's nothing to choose. */
@Data
public class RegisterRiderRequest {
    @NotBlank private String name;
    @NotBlank private String phoneNumber;
    @NotBlank private String password;
    @NotBlank private String idDocumentUrl;
}
