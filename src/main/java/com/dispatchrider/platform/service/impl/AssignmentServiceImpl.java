package com.dispatchrider.platform.service.impl;

import com.dispatchrider.platform.entity.Stop;
import com.dispatchrider.platform.service.AssignmentService;
import com.dispatchrider.platform.service.DistanceMatrixService;
import com.dispatchrider.platform.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final DistanceMatrixService distanceMatrixService;
    private final PricingService pricingService;

    @Override
    public List<Stop> sequenceAndPrice(List<Stop> stops, double pickupLat, double pickupLng) {
        List<Stop> remaining = new ArrayList<>(stops);
        List<Stop> sequenced = new ArrayList<>();

        double currentLat = pickupLat;
        double currentLng = pickupLng;

        int position = 0;
        while (!remaining.isEmpty()) {
            Stop nearest = findNearest(remaining, currentLat, currentLng);
            remaining.remove(nearest);

            pricingService.priceStop(nearest, currentLat, currentLng);
            nearest.setSequenceAssigned(position++);
            sequenced.add(nearest);

            currentLat = nearest.getLat();
            currentLng = nearest.getLng();
        }

        return sequenced;
    }

    @Override
    public List<Stop> applyOverrideSequence(List<Stop> stops, List<Long> stopIdSequence,
                                             double pickupLat, double pickupLng) {
        Map<Long, Stop> byId = stops.stream().collect(Collectors.toMap(Stop::getId, s -> s));

        List<Stop> sequenced = new ArrayList<>();
        double currentLat = pickupLat;
        double currentLng = pickupLng;

        int position = 0;
        for (Long stopId : stopIdSequence) {
            Stop stop = byId.get(stopId);
            if (stop == null) {
                throw new IllegalArgumentException("Stop id " + stopId + " does not belong to this order");
            }
            pricingService.priceStop(stop, currentLat, currentLng);
            stop.setSequenceAssigned(position++);
            sequenced.add(stop);

            currentLat = stop.getLat();
            currentLng = stop.getLng();
        }

        if (sequenced.size() != stops.size()) {
            throw new IllegalArgumentException("Override sequence must include every stop on the order exactly once");
        }

        return sequenced;
    }

    private Stop findNearest(List<Stop> candidates, double fromLat, double fromLng) {
        Stop best = null;
        double bestDist = Double.MAX_VALUE;
        for (Stop candidate : candidates) {
            double d = distanceMatrixService.distanceKm(fromLat, fromLng, candidate.getLat(), candidate.getLng());
            if (d < bestDist) {
                bestDist = d;
                best = candidate;
            }
        }
        return best;
    }
}
