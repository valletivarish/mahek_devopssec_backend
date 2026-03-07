package com.eventmanager.eventrsvp.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for the dashboard endpoint providing summary statistics
 * and chart data for the overview page.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    /** Total number of events in the system */
    private long totalEvents;

    /** Number of upcoming events */
    private long upcomingEvents;

    /** Total number of RSVPs across all events */
    private long totalRsvps;

    /** Total number of confirmed RSVPs */
    private long confirmedRsvps;

    /** Total number of attendees registered */
    private long totalAttendees;

    /** Total number of check-ins recorded */
    private long totalCheckIns;

    /** Overall attendance rate: check-ins divided by confirmed RSVPs, as a percentage */
    private double attendanceRate;

    /** RSVP counts grouped by status (CONFIRMED, DECLINED, MAYBE, WAITLISTED) */
    private Map<String, Long> rsvpsByStatus;

    /** Event counts grouped by category name */
    private Map<String, Long> eventsByCategory;

    /** List of recent events for the dashboard feed */
    private List<EventResponse> recentEvents;

    /** Monthly event counts for the trend chart (month name to count) */
    private Map<String, Long> monthlyEventCounts;
}
