package com.dispatchrider.platform.controller;

import com.dispatchrider.platform.dto.*;
import com.dispatchrider.platform.entity.RiderProfile;
import com.dispatchrider.platform.security.UserPrincipal;
import com.dispatchrider.platform.service.OrderService;
import com.dispatchrider.platform.service.RiderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riders")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;
    private final OrderService orderService;

    @PatchMapping("/me/status") // FR-9a
    public RiderProfileResponse updateMyStatus(@AuthenticationPrincipal UserPrincipal principal,
                                        @Valid @RequestBody RiderStatusUpdateRequest req) {
        return RiderProfileResponse.from(riderService.updateStatus(principal.getUser().getId(), req.getStatus()));
    }

    @GetMapping("/me/orders") // FR-10 - OTPs are never shown to the rider, they only enter them
    public List<OrderResponse> getMyAssignedOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return riderService.getAssignedOrders(principal.getUser().getId()).stream()
                .map(o -> OrderMapper.toResponse(o, false))
                .toList();
    }

    @PatchMapping("/me/stops/{stopId}") // FR-11, FR-12, FR-13 (OTP entry)
    public OrderResponse updateStopStatus(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable Long stopId,
                                           @Valid @RequestBody StopStatusUpdateRequest req) {
        var order = orderService.updateStopStatus(stopId, req, principal.getUser());
        return OrderMapper.toResponse(order, false);
    }
}
