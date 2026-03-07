package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.DashboardResponse;
import com.eventmanager.eventrsvp.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the dashboard endpoint.
 * Aggregates summary statistics and chart data from events, RSVPs, attendees,
 * and check-ins to power the overview dashboard page. This endpoint computes
 * real-time metrics including total counts, attendance rates, RSVP breakdowns
 * by status, event distribution by category, and monthly trend data.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Constructor injection of DashboardService which aggregates
     * data from multiple repositories to build the dashboard response.
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retrieves aggregated dashboard data for the overview page.
     * Returns a comprehensive summary including:
     * - Total event, RSVP, attendee, and check-in counts
     * - Number of upcoming events
     * - Overall attendance rate (check-ins / confirmed RSVPs)
     * - RSVP distribution by status for pie charts
     * - Event distribution by category for bar charts
     * - Recent events list for the activity feed
     * - Monthly event trend data for line charts
     *
     * @return 200 OK with the complete dashboard data payload
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardData() {
        DashboardResponse dashboard = dashboardService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }
}
