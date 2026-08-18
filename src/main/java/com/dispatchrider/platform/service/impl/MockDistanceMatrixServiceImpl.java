package com.dispatchrider.platform.service.impl;

import com.dispatchrider.platform.service.DistanceMatrixService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Free, zero-setup stand-in for the Google Distance Matrix API. Uses Haversine (straight-line)
 * distance multiplied by a road-windiness factor to approximate actual road distance for a
 * dense hyperlocal market. Good enough for pilot-scale dev/demo; swap to the real Google client
 * once app.distance-matrix.use-mock=false and an API key is configured.
 */
@Service
@org.springframework.context.annotation.Primary
public class MockDistanceMatrixServiceImpl implements DistanceMatrixService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Value("${app.distance-matrix.road-factor:1.3}")
    private double roadFactor;

    @Override
    public double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double straightLineKm = haversineKm(lat1, lng1, lat2, lng2);
        return straightLineKm * roadFactor;
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
