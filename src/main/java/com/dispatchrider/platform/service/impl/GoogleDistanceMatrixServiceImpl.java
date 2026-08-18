package com.dispatchrider.platform.service.impl;

import com.dispatchrider.platform.service.DistanceMatrixService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Real Google Distance Matrix API client (NFR-2: free tier comfortably covers pilot volume).
 * Only active when app.distance-matrix.use-mock=false and app.distance-matrix.google-api-key
 * is set - see application.yml. Not marked @Primary so it doesn't get autowired ahead of the
 * mock unless explicitly wired in via ServiceConfig.
 */
@Service("googleDistanceMatrixService")
public class GoogleDistanceMatrixServiceImpl implements DistanceMatrixService {

    private final WebClient webClient;
    private final String apiKey;

    public GoogleDistanceMatrixServiceImpl(
            @Value("${app.distance-matrix.google-base-url}") String baseUrl,
            @Value("${app.distance-matrix.google-api-key}") String apiKey) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    @SuppressWarnings("unchecked")
    public double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "GOOGLE_DISTANCE_MATRIX_API_KEY not configured but use-mock=false");
        }

        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("origins", lat1 + "," + lng1)
                        .queryParam("destinations", lat2 + "," + lng2)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        try {
            var rows = (java.util.List<Map<String, Object>>) response.get("rows");
            var elements = (java.util.List<Map<String, Object>>) rows.get(0).get("elements");
            var distance = (Map<String, Object>) elements.get(0).get("distance");
            double meters = ((Number) distance.get("value")).doubleValue();
            return meters / 1000.0;
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected Distance Matrix API response shape", e);
        }
    }
}
