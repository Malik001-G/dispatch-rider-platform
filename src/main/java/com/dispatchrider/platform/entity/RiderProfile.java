package com.dispatchrider.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * FR-2: identity verification + admin approval before a rider can go ACTIVE.
 * Vehicle type is fixed to bicycle only for Tier 1 (motorcycles excluded - fuel cost makes
 * them unprofitable under the current per-order pricing), so no vehicleType field is needed
 * yet; if Tier 2 (motorcycle, longer-haul) is ever built this is where that field would go.
 */
@Entity
@Table(name = "rider_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id", nullable = false)
    private Hub hub;

    /** Photo/ID uploaded for manual admin verification (FR-2). Stored as a URL/path for MVP. */
    private String idDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RiderApprovalStatus approvalStatus = RiderApprovalStatus.PENDING_REVIEW;

    /**
     * FR-9a: rider manually toggles ACTIVE <-> OFFLINE. System sets BUSY on assignment and
     * back to ACTIVE once all stops on the current order are delivered/failed.
     * A rider can only be set ACTIVE by themselves once approvalStatus = APPROVED.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RiderStatus status = RiderStatus.OFFLINE;

    private Instant approvedAt;
}
