package com.dispatchrider.platform.dto;

import com.dispatchrider.platform.entity.StopStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StopResponse {
    private Long id;
    private Integer sequenceEntered;
    private Integer sequenceAssigned;
    private String recipientName;
    private String recipientPhone;
    private String address;
    private String itemDescription;
    private Double legDistanceKm;
    private Integer feeNaira;
    private StopStatus status;
    private String otp; // only populated in the sender's own view, right after payment (FR-13)
}
