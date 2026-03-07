package com.eventmanager.eventrsvp.dto;

import lombok.*;
import java.util.List;

/**
 * Response DTO for the attendance forecasting endpoint.
 * Contains predicted attendance counts and the regression analysis details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastResponse {

    /** List of predicted attendance values for future events or time periods */
    private List<ForecastPoint> predictions;

    /** Direction of the attendance trend: INCREASING, DECREASING, or STABLE */
    private String trendDirection;

    /** R-squared value indicating how well the model fits historical data (0.0-1.0) */
    private double confidenceScore;

    /** Slope of the regression line indicating rate of change */
    private double slope;

    /** Y-intercept of the regression line */
    private double intercept;

    /** Number of historical data points used for the regression */
    private int dataPointsUsed;

    /**
     * Inner class representing a single forecast data point with a label and predicted value.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForecastPoint {
        /** Label for this prediction point (e.g., month name or event number) */
        private String label;

        /** Predicted attendance count */
        private double predictedAttendance;
    }
}
