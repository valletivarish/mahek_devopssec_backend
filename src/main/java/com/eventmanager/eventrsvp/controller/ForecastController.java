package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.ForecastResponse;
import com.eventmanager.eventrsvp.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the attendance forecasting endpoint.
 * Uses linear regression on historical attendance data to predict future
 * event attendance trends. The forecast helps organisers plan capacity,
 * catering, and staffing for upcoming events based on data-driven predictions.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ForecastController {

    private final ForecastService forecastService;

    /**
     * Constructor injection of ForecastService which implements
     * the linear regression algorithm for attendance prediction.
     */
    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    /**
     * Retrieves the attendance forecast based on historical event data.
     * Performs linear regression analysis on past attendance figures and returns:
     * - Predicted attendance values for future time periods
     * - Trend direction (INCREASING, DECREASING, or STABLE)
     * - Confidence score (R-squared value) indicating model accuracy
     * - Regression parameters (slope and intercept) for transparency
     * - Number of historical data points used in the analysis
     *
     * If insufficient historical data exists (fewer than 2 data points),
     * the response will indicate low confidence and limited predictions.
     *
     * @return 200 OK with the forecast data including predictions and regression metrics
     */
    @GetMapping("/forecast")
    public ResponseEntity<ForecastResponse> getAttendanceForecast() {
        ForecastResponse forecast = forecastService.getAttendanceForecast();
        return ResponseEntity.ok(forecast);
    }
}
