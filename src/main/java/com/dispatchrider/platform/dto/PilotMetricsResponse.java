package com.dispatchrider.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** FR-15 */
@Data
@AllArgsConstructor
public class PilotMetricsResponse {
    private long vendorsOnboarded;
    private long ridersOnboarded;
    private long ordersCompleted;
    private long ordersCancelled;
    private double repeatOrderRatePercent;
    private double averageStopsPerOrder;
}
