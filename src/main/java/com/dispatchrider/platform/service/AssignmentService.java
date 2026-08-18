package com.dispatchrider.platform.service;

import com.dispatchrider.platform.entity.Stop;

import java.util.List;

/**
 * FR-7a: suggests a stop visiting sequence independent of entry order, using nearest-neighbor
 * over API-backed distances. This is explicitly NOT full route optimization (out of scope per
 * BRD 4.2) - it's a greedy "closest unvisited stop next" walk, good enough at pilot scale and
 * cheap to compute. Admin can override the result before confirming assignment (FR-9).
 */
public interface AssignmentService {

    /**
     * Returns stops re-ordered into a suggested visiting sequence (nearest-neighbor from the
     * pickup point), with sequenceAssigned and pricing (legDistanceKm/feeNaira) populated.
     */
    List<Stop> sequenceAndPrice(List<Stop> stops, double pickupLat, double pickupLng);

    /** Applies an admin-provided override sequence (list of stop ids in visiting order) and re-prices. */
    List<Stop> applyOverrideSequence(List<Stop> stops, List<Long> stopIdSequence, double pickupLat, double pickupLng);
}
