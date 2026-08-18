package com.dispatchrider.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * FR-5: recipient + address + item per stop.
 * FR-7/FR-7a: sequenceEntered is the order the sender typed stops in; sequenceAssigned is the
 * nearest-neighbor-suggested (and admin-overridable) visiting order set at assignment time.
 * Pricing is leg-based off sequenceAssigned, not sequenceEntered (BRD 9.1).
 * FR-13: unique OTP per stop, generated on payment confirmation.
 */
@Entity
@Table(name = "stop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Integer sequenceEntered; // 0-based, order sender typed this stop in

    private Integer sequenceAssigned; // 0-based, nullable until assignment (FR-7a)

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(nullable = false)
    private String itemDescription;

    /** Distance in km from the previous stop in the assigned sequence (or from pickup for the first). */
    private Double legDistanceKm;

    /** FR-7: base fee + per-km*legDistance, floored at the configured minimum. */
    private Integer feeNaira;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StopStatus status = StopStatus.PENDING;

    /** FR-13: OTP shown to sender in-app, entered by rider at delivery to confirm the stop. */
    private String otpCode;
    private Instant otpVerifiedAt;

    /** FR-7f: whether this stop's fee has been refunded (rest of order's payment stands). */
    @Builder.Default
    private boolean refunded = false;

    private Instant pickedUpAt;
    private Instant deliveredAt;
    private Instant failedAt;
}
