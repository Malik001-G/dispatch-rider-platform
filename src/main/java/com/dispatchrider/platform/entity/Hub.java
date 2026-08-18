package com.dispatchrider.platform.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * BRD section 5 / NFR-3: coverage rules must be data-driven, not hardcoded, so the platform
 * can support additional hubs later without a redesign - even though only one (Ikorodu Garage)
 * is active at MVP launch.
 */
@Entity
@Table(name = "hub")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double centerLat;

    @Column(nullable = false)
    private Double centerLng;

    @Column(nullable = false)
    private Double radiusKm;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
