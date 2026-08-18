package com.dispatchrider.platform.service;

import com.dispatchrider.platform.entity.Hub;
import com.dispatchrider.platform.repository.HubRepository;
import com.dispatchrider.platform.service.DistanceMatrixService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HubService {

    private final HubRepository hubRepository;
    private final DistanceMatrixService distanceMatrixService;

    public Hub getActiveHub() {
        return hubRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active hub configured"));
    }

    /** FR-6: is this point within the active hub's service radius? */
    public boolean isWithinCoverage(Hub hub, double lat, double lng) {
        double distanceKm = distanceMatrixService.distanceKm(hub.getCenterLat(), hub.getCenterLng(), lat, lng);
        return distanceKm <= hub.getRadiusKm();
    }
}
