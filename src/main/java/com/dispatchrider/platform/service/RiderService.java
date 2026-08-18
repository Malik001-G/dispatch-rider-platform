package com.dispatchrider.platform.service;

import com.dispatchrider.platform.entity.*;
import com.dispatchrider.platform.exception.ApiException;
import com.dispatchrider.platform.repository.OrderRepository;
import com.dispatchrider.platform.repository.RiderProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final RiderProfileRepository riderProfileRepository;
    private final OrderRepository orderRepository;

    /** FR-2: admin approves a pending rider before they can go ACTIVE. */
    @Transactional
    public RiderProfile approveRider(Long riderUserId) {
        RiderProfile profile = riderProfileRepository.findByUserId(riderUserId)
                .orElseThrow(() -> ApiException.notFound("Rider profile not found"));
        profile.setApprovalStatus(RiderApprovalStatus.APPROVED);
        profile.setApprovedAt(Instant.now());
        return riderProfileRepository.save(profile);
    }

    @Transactional
    public RiderProfile rejectRider(Long riderUserId) {
        RiderProfile profile = riderProfileRepository.findByUserId(riderUserId)
                .orElseThrow(() -> ApiException.notFound("Rider profile not found"));
        profile.setApprovalStatus(RiderApprovalStatus.REJECTED);
        return riderProfileRepository.save(profile);
    }

    /**
     * FR-9a: rider manually toggles ACTIVE <-> OFFLINE. Not allowed to self-set BUSY (system
     * controlled), and not allowed to go ACTIVE before admin approval.
     */
    @Transactional
    public RiderProfile updateStatus(Long riderUserId, RiderStatus requested) {
        RiderProfile profile = riderProfileRepository.findByUserId(riderUserId)
                .orElseThrow(() -> ApiException.notFound("Rider profile not found"));

        if (requested == RiderStatus.BUSY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSY is set automatically by the system, not by riders");
        }
        if (requested == RiderStatus.ACTIVE && profile.getApprovalStatus() != RiderApprovalStatus.APPROVED) {
            throw ApiException.forbidden("Rider is not yet approved");
        }
        if (profile.getStatus() == RiderStatus.BUSY) {
            throw ApiException.conflict("Cannot change status while fulfilling an order");
        }

        profile.setStatus(requested);
        return riderProfileRepository.save(profile);
    }

    /** FR-10: rider views their currently assigned order(s). */
    public List<Order> getAssignedOrders(Long riderUserId) {
        return orderRepository.findByAssignedRiderId(riderUserId).stream()
                .filter(o -> o.getStatus() == OrderStatus.ASSIGNED)
                .toList();
    }

    public List<RiderProfile> getPendingApprovals() {
        return riderProfileRepository.findByApprovalStatus(RiderApprovalStatus.PENDING_REVIEW);
    }
}
