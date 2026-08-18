package com.dispatchrider.platform.repository;

import com.dispatchrider.platform.entity.Order;
import com.dispatchrider.platform.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findBySenderId(Long senderId);
    List<Order> findByAssignedRiderId(Long riderId);

    // FR-7b-i: orders stuck PAID with no rider assigned past the auto-refund window
    List<Order> findByStatusAndPaidAtBefore(OrderStatus status, Instant cutoff);

    long countByStatus(OrderStatus status); // FR-15 metrics
}
