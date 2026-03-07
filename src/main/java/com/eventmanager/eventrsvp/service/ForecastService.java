package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.ForecastResponse;
import com.eventmanager.eventrsvp.model.Event;
import com.eventmanager.eventrsvp.model.EventStatus;
import com.eventmanager.eventrsvp.model.RsvpStatus;
import com.eventmanager.eventrsvp.repository.CheckInRepository;
import com.eventmanager.eventrsvp.repository.EventRepository;
import com.eventmanager.eventrsvp.repository.RsvpRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for predicting future event attendance using linear regression.
 *
 * This service implements a simple machine learning approach to attendance
 * forecasting using Apache Commons Math's SimpleRegression class. It analyses
 * historical attendance data from completed events to identify trends and
 * predict attendance for future events.
 *
 * The forecasting algorithm works as follows:
 * 1. Fetches all completed events (events with COMPLETED status)
 * 2. For each completed event, gathers:
 *    - Confirmed RSVP count (how many said they would attend)
 *    - Check-in count (how many actually attended)
 * 3. Fits a simple linear regression model where:
 *    - x = sequential event index (0, 1, 2, ...)
 *    - y = actual attendance (check-in count)
 * 4. Uses the fitted model to predict attendance for the next 3 future events
 *
 * The response includes:
 * - Predictions: list of forecast points with labels and predicted values
 * - Trend direction: INCREASING, DECREASING, or STABLE based on slope
 * - Confidence score: R-squared value (0.0-1.0) indicating model fit quality
 * - Slope and intercept: the regression line parameters
 * - Data points used: number of historical events analysed
 *
 * A minimum of 3 completed events is required for meaningful predictions.
 * With fewer data points, the service returns an empty result with a message.
 */
@Service
public class ForecastService {

    /** Repository for fetching completed events for historical analysis */
    private final EventRepository eventRepository;

    /** Repository for counting confirmed RSVPs per event */
    private final RsvpRepository rsvpRepository;

    /** Repository for counting actual check-ins (attendance) per event */
    private final CheckInRepository checkInRepository;

    /**
     * Constructor injection of all required dependencies.
     *
     * @param eventRepository   repository for event data access
     * @param rsvpRepository    repository for RSVP count queries
     * @param checkInRepository repository for check-in count queries
     */
    public ForecastService(EventRepository eventRepository,
                           RsvpRepository rsvpRepository,
                           CheckInRepository checkInRepository) {
        this.eventRepository = eventRepository;
        this.rsvpRepository = rsvpRepository;
        this.checkInRepository = checkInRepository;
    }

    /**
     * Generates an attendance forecast based on historical data from completed events.
     *
     * The forecasting process follows these steps:
     *
     * Step 1 - Data Collection:
     *   Fetches all events with COMPLETED status from the database. These events
     *   have both RSVP data and check-in data available, making them suitable for
     *   analysing the relationship between planned and actual attendance.
     *
     * Step 2 - Minimum Data Threshold Check:
     *   At least 3 completed events are required to produce a meaningful linear
     *   regression model. With fewer data points, the regression would be either
     *   impossible (0-1 points) or unreliable (2 points give a perfect fit that
     *   likely does not generalise). If insufficient data is available, the method
     *   returns an empty forecast with a descriptive message.
     *
     * Step 3 - Regression Model Training:
     *   Uses Apache Commons Math's SimpleRegression to fit a linear model:
     *     y = slope * x + intercept
     *   where x is the sequential event index and y is the check-in count.
     *   Each completed event contributes one data point to the model.
     *
     * Step 4 - Prediction Generation:
     *   Extrapolates the regression line to predict attendance for the next 3
     *   future events (i.e., events at indices n, n+1, n+2 where n is the number
     *   of historical events). Predicted values are floored at 0 to avoid
     *   negative attendance predictions in declining trends.
     *
     * Step 5 - Trend Analysis:
     *   Determines the trend direction based on the regression slope:
     *   - slope > 0.5: INCREASING (attendance growing significantly)
     *   - slope < -0.5: DECREASING (attendance declining significantly)
     *   - otherwise: STABLE (no significant change)
     *   The 0.5 threshold provides a buffer to avoid labelling minor
     *   fluctuations as meaningful trends.
     *
     * Step 6 - Confidence Assessment:
     *   The R-squared value (coefficient of determination) indicates how well
     *   the linear model fits the historical data:
     *   - 1.0 = perfect fit (all data points on the line)
     *   - 0.0 = model explains none of the variance
     *   - Negative values are clamped to 0.0 (can occur with very poor fits)
     *
     * @return a ForecastResponse containing predictions, trend analysis, and model statistics
     */
    @Transactional(readOnly = true)
    public ForecastResponse getAttendanceForecast() {
        // Step 1: Fetch all completed events as the historical dataset.
        // Only completed events have reliable attendance data since their
        // check-in process has finished.
        List<Event> completedEvents = eventRepository.findByStatus(EventStatus.COMPLETED);

        // Step 2: Check if we have enough data points for a meaningful regression.
        // A minimum of 3 data points is required to produce a regression that
        // is not trivially overfitting (2 points always give R-squared = 1.0).
        if (completedEvents.size() < 3) {
            // Return an empty forecast response indicating insufficient data.
            // The frontend should display a message to the user explaining that
            // more completed events are needed for predictions.
            return ForecastResponse.builder()
                    .predictions(Collections.emptyList())
                    .trendDirection("INSUFFICIENT_DATA")
                    .confidenceScore(0.0)
                    .slope(0.0)
                    .intercept(0.0)
                    .dataPointsUsed(completedEvents.size())
                    .build();
        }

        // Step 3: Build the regression model from historical attendance data.
        // SimpleRegression fits the equation y = slope * x + intercept using
        // the ordinary least squares method.
        SimpleRegression regression = new SimpleRegression();

        for (int i = 0; i < completedEvents.size(); i++) {
            Event event = completedEvents.get(i);

            // Get the actual attendance (check-in count) for this completed event.
            // This is the "ground truth" we are trying to model and predict.
            long checkInCount = checkInRepository.countByEventId(event.getId());

            // Add the data point to the regression model.
            // x = sequential index (ordering events chronologically by position)
            // y = actual attendance count from check-ins
            regression.addData(i, checkInCount);
        }

        // Step 4: Generate predictions for the next 3 future events.
        // Extrapolate the regression line beyond the historical data range.
        int numberOfPredictions = 3;
        int startIndex = completedEvents.size();
        List<ForecastResponse.ForecastPoint> predictions = new ArrayList<>();

        for (int i = 0; i < numberOfPredictions; i++) {
            int futureIndex = startIndex + i;

            // Use the regression model to predict attendance at the future index.
            // Ensure the predicted value is not negative (attendance cannot be < 0).
            double predictedAttendance = Math.max(0, regression.predict(futureIndex));

            // Create a labelled forecast point for the response.
            // Labels indicate which future event the prediction is for.
            ForecastResponse.ForecastPoint point = ForecastResponse.ForecastPoint.builder()
                    .label("Future Event " + (i + 1))
                    .predictedAttendance(Math.round(predictedAttendance * 100.0) / 100.0)
                    .build();

            predictions.add(point);
        }

        // Step 5: Determine the trend direction from the regression slope.
        // The slope indicates the average change in attendance per event:
        // - Positive slope: attendance is generally increasing over time
        // - Negative slope: attendance is generally decreasing over time
        // - Near-zero slope: attendance is relatively stable
        double slope = regression.getSlope();
        String trendDirection;
        if (slope > 0.5) {
            trendDirection = "INCREASING";
        } else if (slope < -0.5) {
            trendDirection = "DECREASING";
        } else {
            trendDirection = "STABLE";
        }

        // Step 6: Calculate the confidence score (R-squared).
        // R-squared measures how well the linear model explains the variance
        // in the historical data. Values closer to 1.0 indicate a better fit.
        // Clamp negative values to 0.0 since negative R-squared indicates the
        // model is worse than a horizontal line at the mean.
        double rSquared = regression.getRSquare();
        double confidenceScore = Double.isNaN(rSquared) ? 0.0 : Math.max(0.0, rSquared);

        // Build and return the complete forecast response
        return ForecastResponse.builder()
                .predictions(predictions)
                .trendDirection(trendDirection)
                .confidenceScore(Math.round(confidenceScore * 10000.0) / 10000.0)
                .slope(Math.round(slope * 10000.0) / 10000.0)
                .intercept(Math.round(regression.getIntercept() * 10000.0) / 10000.0)
                .dataPointsUsed(completedEvents.size())
                .build();
    }
}
