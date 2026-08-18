package com.dispatchrider.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Shared account entity for FR-1 (business owner registration), FR-2 (rider registration),
 * and FR-3 (login). Role-specific fields that only make sense for one role live directly here
 * for MVP simplicity (see 4.2 - no need for full role hierarchy tables yet); rider-only
 * verification/status fields live on {@link RiderProfile}.
 *
 * Registration eligibility is geography-based, not role-restricted: anyone inside a hub's
 * radius can register as either a business owner or a rider.
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- Business owner (sender) fields - FR-1 ---
    private String shopLocationAddress;
    private Double shopLat;
    private Double shopLng;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id")
    private Hub hub;

    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
