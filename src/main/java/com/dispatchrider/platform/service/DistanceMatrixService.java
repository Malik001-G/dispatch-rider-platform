package com.dispatchrider.platform.service;

/**
 * Backs FR-6 (radius validation), FR-7 (leg-based pricing), and FR-7a (nearest-neighbor
 * sequencing) - all three need real road distance, not straight-line distance, per the BRD's
 * explicit "not straight-line distance" requirement.
 *
 * Two implementations: a free Haversine-based mock for local dev (NFR-2: no paid API needed
 * until pilot volume justifies it), and a real Google Distance Matrix client for when
 * app.distance-matrix.use-mock=false.
 */
public interface DistanceMatrixService {
    /** Road distance in kilometers between two points. */
    double distanceKm(double lat1, double lng1, double lat2, double lng2);
}
