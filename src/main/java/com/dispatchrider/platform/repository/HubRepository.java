package com.dispatchrider.platform.repository;

import com.dispatchrider.platform.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HubRepository extends JpaRepository<Hub, Long> {
    Optional<Hub> findFirstByActiveTrue();
}
