package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.ForecastResponse;
import com.eventmanager.eventrsvp.model.*;
import com.eventmanager.eventrsvp.repository.CheckInRepository;
import com.eventmanager.eventrsvp.repository.EventRepository;
import com.eventmanager.eventrsvp.repository.RsvpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the ForecastService that uses Apache Commons Math SimpleRegression
 * to predict future event attendance based on historical data.
 * Uses Mockito to mock repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RsvpRepository rsvpRepository;

    @Mock
    private CheckInRepository checkInRepository;

    @InjectMocks
    private ForecastService forecastService;

    /**
     * Tests that the forecast returns predictions when sufficient historical data exists.
     * Creates mock completed events with check-in data to feed the regression model.
     */
    @Test
    void getAttendanceForecastWithSufficientDataShouldReturnPredictions() {
        // Create mock completed events with increasing attendance
        Event event1 = createMockEvent(1L, "Event 1", EventStatus.COMPLETED);
        Event event2 = createMockEvent(2L, "Event 2", EventStatus.COMPLETED);
        Event event3 = createMockEvent(3L, "Event 3", EventStatus.COMPLETED);
        Event event4 = createMockEvent(4L, "Event 4", EventStatus.COMPLETED);

        when(eventRepository.findByStatus(EventStatus.COMPLETED))
                .thenReturn(Arrays.asList(event1, event2, event3, event4));

        // Mock increasing check-in counts to establish an upward trend
        when(checkInRepository.countByEventId(1L)).thenReturn(20L);
        when(checkInRepository.countByEventId(2L)).thenReturn(35L);
        when(checkInRepository.countByEventId(3L)).thenReturn(45L);
        when(checkInRepository.countByEventId(4L)).thenReturn(60L);

        ForecastResponse response = forecastService.getAttendanceForecast();

        assertNotNull(response, "Forecast response should not be null");
        assertFalse(response.getPredictions().isEmpty(), "Should have prediction data points");
        assertEquals("INCREASING", response.getTrendDirection(),
                "Trend should be increasing with rising attendance numbers");
        assertTrue(response.getDataPointsUsed() >= 3,
                "Should use at least 3 data points for regression");
    }

    /**
     * Tests that the forecast handles insufficient data gracefully
     * when fewer than 3 completed events exist.
     */
    @Test
    void getAttendanceForecastWithInsufficientDataShouldReturnEmpty() {
        // Only 2 completed events - not enough for meaningful regression
        Event event1 = createMockEvent(1L, "Event 1", EventStatus.COMPLETED);
        Event event2 = createMockEvent(2L, "Event 2", EventStatus.COMPLETED);

        when(eventRepository.findByStatus(EventStatus.COMPLETED))
                .thenReturn(Arrays.asList(event1, event2));
        when(checkInRepository.countByEventId(anyLong())).thenReturn(10L);

        ForecastResponse response = forecastService.getAttendanceForecast();

        assertNotNull(response, "Response should not be null even with insufficient data");
        assertTrue(response.getPredictions().isEmpty() || response.getDataPointsUsed() < 3,
                "Should indicate insufficient data for reliable predictions");
    }

    /**
     * Tests forecast with no completed events returns empty predictions.
     */
    @Test
    void getAttendanceForecastWithNoEventsShouldReturnEmpty() {
        when(eventRepository.findByStatus(EventStatus.COMPLETED))
                .thenReturn(Collections.emptyList());

        ForecastResponse response = forecastService.getAttendanceForecast();

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getPredictions().isEmpty(),
                "No predictions possible without historical data");
    }

    /**
     * Helper method to create a mock Event object for testing.
     */
    private Event createMockEvent(Long id, String title, EventStatus status) {
        Event event = new Event();
        event.setId(id);
        event.setTitle(title);
        event.setStatus(status);
        event.setEventDate(LocalDate.now().minusDays(30 - id));
        event.setStartTime(LocalTime.of(9, 0));
        event.setEndTime(LocalTime.of(17, 0));
        event.setCapacity(100);
        return event;
    }
}
