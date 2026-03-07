package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.DashboardResponse;
import com.eventmanager.eventrsvp.dto.EventResponse;
import com.eventmanager.eventrsvp.model.Category;
import com.eventmanager.eventrsvp.model.Event;
import com.eventmanager.eventrsvp.model.EventStatus;
import com.eventmanager.eventrsvp.model.RsvpStatus;
import com.eventmanager.eventrsvp.repository.AttendeeRepository;
import com.eventmanager.eventrsvp.repository.CategoryRepository;
import com.eventmanager.eventrsvp.repository.CheckInRepository;
import com.eventmanager.eventrsvp.repository.EventRepository;
import com.eventmanager.eventrsvp.repository.RsvpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregation service for the dashboard endpoint.
 *
 * The dashboard provides a consolidated overview of the entire event management
 * system, including summary statistics, distribution charts, and recent activity.
 * This service aggregates data from multiple repositories to build a comprehensive
 * response that powers the frontend dashboard page.
 *
 * The dashboard response includes:
 * - Summary cards: total events, upcoming events, total RSVPs, confirmed RSVPs,
 *   total attendees, total check-ins, and the overall attendance rate
 * - RSVP distribution: a breakdown of RSVPs by status for a pie/donut chart
 * - Category distribution: event counts per category for a bar chart
 * - Recent events: the 5 most recently created events for a feed/timeline
 * - Monthly trends: event counts per month for the last 12 months for a line chart
 *
 * All operations are read-only, so @Transactional(readOnly = true) is applied at
 * the method level to optimise database performance.
 */
@Service
public class DashboardService {

    /** Repository for event queries (total count, status count, recent events, monthly trends) */
    private final EventRepository eventRepository;

    /** Repository for RSVP queries (total count, status breakdown) */
    private final RsvpRepository rsvpRepository;

    /** Repository for attendee count */
    private final AttendeeRepository attendeeRepository;

    /** Repository for check-in count (actual attendance) */
    private final CheckInRepository checkInRepository;

    /** Repository for category listing (events-by-category distribution) */
    private final CategoryRepository categoryRepository;

    /**
     * Constructor injection of all required repositories.
     *
     * @param eventRepository    repository for event data
     * @param rsvpRepository     repository for RSVP statistics
     * @param attendeeRepository repository for attendee count
     * @param checkInRepository  repository for check-in count
     * @param categoryRepository repository for category listing
     */
    public DashboardService(EventRepository eventRepository,
                            RsvpRepository rsvpRepository,
                            AttendeeRepository attendeeRepository,
                            CheckInRepository checkInRepository,
                            CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.rsvpRepository = rsvpRepository;
        this.attendeeRepository = attendeeRepository;
        this.checkInRepository = checkInRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Builds and returns the complete dashboard data response.
     *
     * This method aggregates data from five different repositories to construct
     * a comprehensive dashboard view. The aggregation includes:
     *
     * 1. Summary Statistics:
     *    - totalEvents: count of all events regardless of status
     *    - upcomingEvents: count of events with UPCOMING status
     *    - totalRsvps: count of all RSVP records across all events
     *    - confirmedRsvps: count of RSVPs with CONFIRMED status
     *    - totalAttendees: count of all registered attendees
     *    - totalCheckIns: count of all check-in records
     *
     * 2. Attendance Rate Calculation:
     *    Computed as (totalCheckIns / confirmedRsvps) * 100 to get a percentage.
     *    If there are no confirmed RSVPs, the rate defaults to 0.0 to avoid
     *    division by zero. This metric shows what percentage of confirmed
     *    attendees actually showed up.
     *
     * 3. RSVP Status Distribution:
     *    Counts RSVPs for each status (CONFIRMED, DECLINED, MAYBE, WAITLISTED)
     *    to power a pie or donut chart showing the breakdown of responses.
     *
     * 4. Events by Category Distribution:
     *    Counts events in each category to show which topics are most popular.
     *    This uses all existing categories and counts events per category.
     *
     * 5. Recent Events:
     *    The 5 most recently created events, mapped to EventResponse DTOs
     *    with confirmed RSVP counts for a dashboard activity feed.
     *
     * 6. Monthly Event Counts:
     *    Event counts for each of the last 12 months, providing trend data
     *    for a line chart. The months are ordered chronologically and labelled
     *    with "MMM yyyy" format (e.g., "Jan 2026").
     *
     * @return a DashboardResponse containing all aggregated dashboard data
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        // --- Summary Statistics ---
        // Count total events across all statuses
        long totalEvents = eventRepository.count();

        // Count events that are scheduled to happen in the future
        long upcomingEvents = eventRepository.countByStatus(EventStatus.UPCOMING);

        // Count all RSVP records to show overall engagement
        long totalRsvps = rsvpRepository.count();

        // Count confirmed RSVPs for attendance rate calculation
        long confirmedRsvps = rsvpRepository.countByStatus(RsvpStatus.CONFIRMED);

        // Count all registered attendees in the system
        long totalAttendees = attendeeRepository.count();

        // Count all check-in records representing actual event attendance
        long totalCheckIns = checkInRepository.count();

        // --- Attendance Rate ---
        // Calculate the percentage of confirmed attendees who actually checked in.
        // This is a key metric for event organisers to understand no-show rates.
        // Guard against division by zero when there are no confirmed RSVPs.
        double attendanceRate = confirmedRsvps > 0
                ? ((double) totalCheckIns / confirmedRsvps) * 100.0
                : 0.0;

        // --- RSVP Status Distribution ---
        // Build a map of RSVP status name to count for the distribution chart.
        // Iterates through all possible RsvpStatus enum values to ensure every
        // status is represented, even those with zero count.
        Map<String, Long> rsvpsByStatus = new LinkedHashMap<>();
        for (RsvpStatus status : RsvpStatus.values()) {
            rsvpsByStatus.put(status.name(), rsvpRepository.countByStatus(status));
        }

        // --- Events by Category Distribution ---
        // Build a map of category name to event count for the category chart.
        // Fetches all categories and counts events in each one, providing a
        // complete picture even for categories with no events.
        Map<String, Long> eventsByCategory = new LinkedHashMap<>();
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            long eventCount = eventRepository.countByCategoryId(category.getId());
            eventsByCategory.put(category.getName(), eventCount);
        }

        // --- Recent Events ---
        // Fetch the 5 most recently created events and map them to response DTOs.
        // Each response includes the confirmed RSVP count for quick reference.
        List<EventResponse> recentEvents = eventRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapEventToResponse)
                .collect(Collectors.toList());

        // --- Monthly Event Counts ---
        // Calculate event counts for each of the last 12 months to power
        // a trend chart. We iterate backwards from the current month and
        // count events whose eventDate falls within each month's range.
        Map<String, Long> monthlyEventCounts = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            // Calculate the first and last day of the target month
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            // Format the month label as "MMM yyyy" (e.g., "Mar 2026")
            String monthLabel = monthStart.getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + monthStart.getYear();

            // Count events whose event date falls within this month
            long count = eventRepository.findByEventDateBetween(monthStart, monthEnd).size();
            monthlyEventCounts.put(monthLabel, count);
        }

        // --- Build and Return the Dashboard Response ---
        return DashboardResponse.builder()
                .totalEvents(totalEvents)
                .upcomingEvents(upcomingEvents)
                .totalRsvps(totalRsvps)
                .confirmedRsvps(confirmedRsvps)
                .totalAttendees(totalAttendees)
                .totalCheckIns(totalCheckIns)
                .attendanceRate(attendanceRate)
                .rsvpsByStatus(rsvpsByStatus)
                .eventsByCategory(eventsByCategory)
                .recentEvents(recentEvents)
                .monthlyEventCounts(monthlyEventCounts)
                .build();
    }

    /**
     * Maps an Event entity to an EventResponse DTO with the confirmed RSVP count.
     *
     * This is a private helper method used specifically within the dashboard context
     * to convert recent events to their response representation. The confirmedCount
     * is included so the dashboard feed can show capacity utilisation for each event.
     *
     * @param event the Event entity to convert
     * @return the corresponding EventResponse DTO
     */
    private EventResponse mapEventToResponse(Event event) {
        // Query the confirmed RSVP count to include in the response
        long confirmedCount = rsvpRepository.countByEventIdAndStatus(
                event.getId(), RsvpStatus.CONFIRMED);

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .capacity(event.getCapacity())
                .status(event.getStatus())
                .organizerId(event.getOrganizer().getId())
                .organizerName(event.getOrganizer().getFullName())
                .categoryId(event.getCategory().getId())
                .categoryName(event.getCategory().getName())
                .categoryColorCode(event.getCategory().getColorCode())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .confirmedCount(confirmedCount)
                .build();
    }
}
