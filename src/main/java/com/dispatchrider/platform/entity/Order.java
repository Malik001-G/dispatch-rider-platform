package com.dispatchrider.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * FR-4: one pickup, multiple stops. FR-14: fields needed for pilot reporting live directly
 * on the order (stop count derived from {@link #stops}, fee, status, timestamps).
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hub_id", nullable = false)
    private Hub hub;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private Double pickupLat;

    @Column(nullable = false)
    private Double pickupLng;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("sequenceEntered ASC")
    private List<Stop> stops = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_rider_id")
    private User assignedRider; // User with role RIDER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    /** FR-7: base pickup fee, order-level (see BRD 9.1 worked example). */
    @Column(nullable = false)
    private Integer pickupFeeNaira;

    /** Sum of pickupFeeNaira + all stop fees, in naira. FR-14 reporting field. */
    @Column(nullable = false)
    private Integer totalFeeNaira;

    private String paystackReference;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant paidAt;
    private Instant assignedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    public int getStopCount() {
        return stops == null ? 0 : stops.size();
    }
}
