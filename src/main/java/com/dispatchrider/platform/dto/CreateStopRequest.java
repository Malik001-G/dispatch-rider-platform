package com.dispatchrider.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** One entry in a CreateOrderRequest - FR-5 */
@Data
public class CreateStopRequest {
    @NotBlank private String recipientName;
    @NotBlank private String recipientPhone;
    @NotBlank private String address;
    private Double lat;
    private Double lng;
    @NotBlank private String itemDescription;
}
