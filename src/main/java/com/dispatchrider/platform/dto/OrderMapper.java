package com.dispatchrider.platform.dto;

import com.dispatchrider.platform.entity.Order;
import com.dispatchrider.platform.entity.Stop;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FR-13: OTPs are only ever shown to the sender who owns the order (they're the one who
 * forwards each OTP to its recipient off-platform) - includeOtp must be false for any other
 * viewer, including the assigned rider, who only enters the OTP rather than seeing it.
 */
public final class OrderMapper {

    private OrderMapper() {}

    public static OrderResponse toResponse(Order order, boolean includeOtp) {
        List<StopResponse> stops = order.getStops().stream()
                .map(s -> toStopResponse(s, includeOtp))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getPickupAddress(),
                order.getStatus(),
                order.getPickupFeeNaira(),
                order.getTotalFeeNaira(),
                order.getAssignedRider() != null ? order.getAssignedRider().getId() : null,
                stops,
                order.getCreatedAt()
        );
    }

    public static StopResponse toStopResponse(Stop s, boolean includeOtp) {
        return new StopResponse(
                s.getId(),
                s.getSequenceEntered(),
                s.getSequenceAssigned(),
                s.getRecipientName(),
                s.getRecipientPhone(),
                s.getAddress(),
                s.getItemDescription(),
                s.getLegDistanceKm(),
                s.getFeeNaira(),
                s.getStatus(),
                includeOtp ? s.getOtpCode() : null
        );
    }
}
