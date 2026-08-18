package com.dispatchrider.platform.scheduler;

import com.dispatchrider.platform.entity.Order;
import com.dispatchrider.platform.entity.OrderStatus;
import com.dispatchrider.platform.repository.OrderRepository;
import com.dispatchrider.platform.service.PaystackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * FR-7b-i: two orders could both pass the "at least one ACTIVE rider" check (FR-7b) before
 * either is actually assigned - a race condition. Rather than let a PAID-but-unassigned order
 * sit unfulfilled, this job runs periodically and auto-refunds + cancels any order that has
 * been PAID for longer than the configured window without being assigned a rider.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnassignedOrderRefundScheduler {

    private final OrderRepository orderRepository;
    private final PaystackService paystackService;

    @Value("${app.assignment.unassigned-payment-window-minutes}")
    private long windowMinutes;

    @Scheduled(fixedDelayString = "PT1M") // check every minute; cheap query at pilot volume
    @Transactional
    public void refundStaleUnassignedOrders() {
        Instant cutoff = Instant.now().minus(windowMinutes, ChronoUnit.MINUTES);
        List<Order> stale = orderRepository.findByStatusAndPaidAtBefore(OrderStatus.PAID, cutoff);

        for (Order order : stale) {
            try {
                paystackService.refund(order.getPaystackReference(), null);
                order.setStatus(OrderStatus.CANCELLED_NO_RIDER);
                order.setCancelledAt(Instant.now());
                orderRepository.save(order);
                log.info("Auto-refunded and cancelled order {} - no rider assigned within {} minutes",
                        order.getId(), windowMinutes);
            } catch (Exception e) {
                log.error("Failed to auto-refund order {}: {}", order.getId(), e.getMessage());
                // Left as PAID; will be retried on the next run and is visible to admin either way.
            }
        }
    }
}
