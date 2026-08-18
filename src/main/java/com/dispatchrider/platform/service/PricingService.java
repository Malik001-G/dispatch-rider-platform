package com.dispatchrider.platform.service;

import com.dispatchrider.platform.entity.Stop;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * FR-7: leg-based pricing. Fee for each stop = base fee + per-km * distance from the *previous*
 * point in the sequenced route (not straight-line from pickup), floored at a minimum.
 * Matches BRD 9.1 worked example: base 500 + 100/km, 800 floor.
 */
@Service
public class PricingService {

    private final DistanceMatrixService distanceMatrixService;
    private final int pickupFeeNaira;
    private final int stopBaseFeeNaira;
    private final int perKmRateNaira;
    private final int stopFeeFloorNaira;

    public PricingService(DistanceMatrixService distanceMatrixService,
                           @Value("${app.pricing.pickup-fee-naira}") int pickupFeeNaira,
                           @Value("${app.pricing.stop-base-fee-naira}") int stopBaseFeeNaira,
                           @Value("${app.pricing.per-km-rate-naira}") int perKmRateNaira,
                           @Value("${app.pricing.stop-fee-floor-naira}") int stopFeeFloorNaira) {
        this.distanceMatrixService = distanceMatrixService;
        this.pickupFeeNaira = pickupFeeNaira;
        this.stopBaseFeeNaira = stopBaseFeeNaira;
        this.perKmRateNaira = perKmRateNaira;
        this.stopFeeFloorNaira = stopFeeFloorNaira;
    }

    public int getPickupFeeNaira() {
        return pickupFeeNaira;
    }

    /**
     * Prices one stop given the lat/lng of the previous point in the sequenced route
     * (the pickup point for the first stop, the prior stop for every subsequent one).
     * Mutates the stop's legDistanceKm and feeNaira fields.
     */
    public void priceStop(Stop stop, double prevLat, double prevLng) {
        double legKm = distanceMatrixService.distanceKm(prevLat, prevLng, stop.getLat(), stop.getLng());
        int fee = stopBaseFeeNaira + (int) Math.round(legKm * perKmRateNaira);
        fee = Math.max(fee, stopFeeFloorNaira);

        stop.setLegDistanceKm(legKm);
        stop.setFeeNaira(fee);
    }
}
