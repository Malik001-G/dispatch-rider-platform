package com.dispatchrider.platform.controller;

import com.dispatchrider.platform.dto.*;
import com.dispatchrider.platform.entity.Order;
import com.dispatchrider.platform.security.UserPrincipal;
import com.dispatchrider.platform.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping // FR-4, FR-5, FR-6, FR-7
    public OrderResponse createOrder(@AuthenticationPrincipal UserPrincipal principal,
                                      @Valid @RequestBody CreateOrderRequest req) {
        Order order = orderService.createOrder(principal.getUser(), req);
        return OrderMapper.toResponse(order, true);
    }

    @PostMapping("/{orderId}/pay") // FR-7b, FR-7c - returns the Paystack authorization URL
    public Map<String, Object> initiatePayment(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long orderId) {
        return orderService.initiatePayment(orderId, principal.getUser());
    }

    @PostMapping("/{orderId}/cancel") // FR-7g
    public void cancelOrder(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long orderId) {
        orderService.cancelOrder(orderId, principal.getUser());
    }

    @GetMapping("/{orderId}") // FR-13: shows OTPs, since this is the sender viewing their own order
    public OrderResponse getOrder(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long orderId) {
        return OrderMapper.toResponse(orderService.getOrderForSender(orderId, principal.getUser()), true);
    }

    @GetMapping
    public java.util.List<OrderResponse> getMyOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return orderService.getOrdersForSender(principal.getUser()).stream()
                .map(o -> OrderMapper.toResponse(o, true))
                .toList();
    }
}
