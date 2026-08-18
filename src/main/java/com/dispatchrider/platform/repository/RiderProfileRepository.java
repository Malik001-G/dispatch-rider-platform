package com.dispatchrider.platform.repository;

import com.dispatchrider.platform.entity.RiderApprovalStatus;
import com.dispatchrider.platform.entity.RiderProfile;
import com.dispatchrider.platform.entity.RiderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiderProfileRepository extends JpaRepository<RiderProfile, Long> {
    Optional<RiderProfile> findByUserId(Long userId);
    List<RiderProfile> findByHubIdAndStatus(Long hubId, RiderStatus status);
    List<RiderProfile> findByApprovalStatus(RiderApprovalStatus status);
    boolean existsByHubIdAndStatus(Long hubId, RiderStatus status); // backs FR-7b
}
