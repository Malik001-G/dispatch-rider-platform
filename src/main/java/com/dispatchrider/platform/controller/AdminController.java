package com.dispatchrider.platform.controller;

import com.dispatchrider.platform.dto.*;
import com.dispatchrider.platform.entity.*;
import com.dispatchrider.platform.repository.OrderRepository;
import com.dispatchrider.platform.repository.RiderProfileRepository;
import com.dispatchrider.platform.service.HubService;
import com.dispatchrider.platform.service.OrderService;
import com.dispatchrider.platform.service.ReportingService;
import com.dispatchrider.platform.service.RiderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final RiderService riderService;
    private final ReportingService reportingService;
    private final OrderRepository orderRepository;
    private final RiderProfileRepository riderProfileRepository;
    private final HubService hubService;

    // ---------- FR-8: pending orders + available riders ----------

    @GetMapping("/orders/pending") // PAID, awaiting assignment
    public List<OrderResponse> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PAID).stream()
                .map(o -> OrderMapper.toResponse(o, false))
                .toList();
    }

    @GetMapping("/riders/available")
    public List<RiderProfileResponse> getAvailableRiders() {
        var hub = hubService.getActiveHub();
        return riderProfileRepository.findByHubIdAndStatus(hub.getId(), RiderStatus.ACTIVE)
                .stream().map(RiderProfileResponse::from).toList();
    }

    // ---------- FR-9: manual assignment ----------

    @PostMapping("/orders/{orderId}/assign")
    public OrderResponse assignRider(@PathVariable Long orderId, @Valid @RequestBody AssignRiderRequest req) {
        var order = orderService.assignRider(orderId, req);
        return OrderMapper.toResponse(order, false);
    }

    // ---------- FR-2: rider approval ----------

    @GetMapping("/riders/pending-approval")
    public List<RiderProfileResponse> getPendingApprovals() {
        return riderService.getPendingApprovals().stream().map(RiderProfileResponse::from).toList();
    }

    @PostMapping("/riders/{riderUserId}/approve")
    public RiderProfileResponse approveRider(@PathVariable Long riderUserId) {
        return RiderProfileResponse.from(riderService.approveRider(riderUserId));
    }

    @PostMapping("/riders/{riderUserId}/reject")
    public RiderProfileResponse rejectRider(@PathVariable Long riderUserId) {
        return RiderProfileResponse.from(riderService.rejectRider(riderUserId));
    }

    // ---------- FR-15: pilot metrics ----------

    @GetMapping("/metrics")
    public PilotMetricsResponse getMetrics() {
        return reportingService.getPilotMetrics();
    }
}
