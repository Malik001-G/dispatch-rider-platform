package com.dispatchrider.platform.repository;

import com.dispatchrider.platform.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {
    List<Stop> findByOrderId(Long orderId);
}
