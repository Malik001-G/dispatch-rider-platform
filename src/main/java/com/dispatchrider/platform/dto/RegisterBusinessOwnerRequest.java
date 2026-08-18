package com.dispatchrider.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** FR-1 */
@Data
public class RegisterBusinessOwnerRequest {
    @NotBlank private String name;
    @NotBlank private String phoneNumber;
    @NotBlank private String password;
    @NotBlank private String shopLocationAddress;
    private Double shopLat;
    private Double shopLng;
}
