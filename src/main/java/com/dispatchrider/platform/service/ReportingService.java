package com.dispatchrider.platform.service;

import com.dispatchrider.platform.dto.PilotMetricsResponse;
import com.dispatchrider.platform.entity.Order;
import com.dispatchrider.platform.entity.OrderStatus;
import com.dispatchrider.platform.entity.Role;
import com.dispatchrider.platform.repository.OrderRepository;
import com.dispatchrider.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** FR-14/FR-15: vendors onboarded, orders completed, repeat-order rate, average stops per order. */
@Service
@RequiredArgsConstructor
public class ReportingService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PilotMetricsResponse getPilotMetrics() {
        long vendors = userRepository.countByRole(Role.BUSINESS_OWNER);
        long riders = userRepository.countByRole(Role.RIDER);
        long completed = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long cancelled = orderRepository.countByStatus(OrderStatus.CANCELLED)
                + orderRepository.countByStatus(OrderStatus.CANCELLED_NO_RIDER);

        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED);

        double avgStops = completedOrders.isEmpty() ? 0.0
                : completedOrders.stream().mapToInt(Order::getStopCount).average().orElse(0.0);

        // Repeat-order rate: % of senders with a completed order who have more than one.
        Map<Long, Long> ordersPerSender = completedOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getSender().getId(), Collectors.counting()));
        long sendersWithRepeat = ordersPerSender.values().stream().filter(c -> c > 1).count();
        double repeatRate = ordersPerSender.isEmpty() ? 0.0
                : (100.0 * sendersWithRepeat / ordersPerSender.size());

        return new PilotMetricsResponse(vendors, riders, completed, cancelled, repeatRate, avgStops);
    }
}
