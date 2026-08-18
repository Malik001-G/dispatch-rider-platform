package com.dispatchrider.platform.dto;

import com.dispatchrider.platform.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String pickupAddress;
    private OrderStatus status;
    private Integer pickupFeeNaira;
    private Integer totalFeeNaira;
    private Long assignedRiderId;
    private List<StopResponse> stops;
    private Instant createdAt;
}
