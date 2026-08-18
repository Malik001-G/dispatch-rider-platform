package com.dispatchrider.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

/** FR-4: one pickup, N stops, entered in whatever order the sender types them in. */
@Data
public class CreateOrderRequest {
    @NotBlank private String pickupAddress;
    private Double pickupLat;
    private Double pickupLng;

    @NotEmpty
    @Valid
    private List<CreateStopRequest> stops;
}
