package com.dispatchrider.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** FR-3 */
@Data
public class LoginRequest {
    @NotBlank private String phoneNumber;
    @NotBlank private String password;
}
