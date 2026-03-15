package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.DashboardResponse;
import com.eventmanager.eventrsvp.model.*;
import com.eventmanager.eventrsvp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private RsvpRepository rsvpRepository;
    @Mock
    private AttendeeRepository attendeeRepository;
    @Mock
    private CheckInRepository checkInRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        // Common stubs
    }

    @Test
    void getDashboardDataShouldReturnCompleteResponse() {
        when(eventRepository.count()).thenReturn(10L);
        when(eventRepository.countByStatus(EventStatus.UPCOMING)).thenReturn(5L);
        when(rsvpRepository.count()).thenReturn(50L);
        when(rsvpRepository.countByStatus(RsvpStatus.CONFIRMED)).thenReturn(30L);
        when(rsvpRepository.countByStatus(RsvpStatus.DECLINED)).thenReturn(5L);
        when(rsvpRepository.countByStatus(RsvpStatus.MAYBE)).thenReturn(10L);
        when(rsvpRepository.countByStatus(RsvpStatus.WAITLISTED)).thenReturn(5L);
        when(attendeeRepository.count()).thenReturn(25L);
        when(checkInRepository.count()).thenReturn(20L);
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(eventRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        when(eventRepository.findByEventDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        DashboardResponse result = dashboardService.getDashboardData();

        assertNotNull(result);
        assertEquals(10L, result.getTotalEvents());
        assertEquals(5L, result.getUpcomingEvents());
        assertEquals(50L, result.getTotalRsvps());
        assertEquals(30L, result.getConfirmedRsvps());
        assertEquals(25L, result.getTotalAttendees());
        assertEquals(20L, result.getTotalCheckIns());
        assertTrue(result.getAttendanceRate() > 0);
        assertNotNull(result.getRsvpsByStatus());
        assertEquals(4, result.getRsvpsByStatus().size());
        assertNotNull(result.getMonthlyEventCounts());
        assertEquals(12, result.getMonthlyEventCounts().size());
    }

    @Test
    void getDashboardDataShouldHandleZeroConfirmedRsvps() {
        when(eventRepository.count()).thenReturn(0L);
        when(eventRepository.countByStatus(EventStatus.UPCOMING)).thenReturn(0L);
        when(rsvpRepository.count()).thenReturn(0L);
        when(rsvpRepository.countByStatus(any(RsvpStatus.class))).thenReturn(0L);
        when(attendeeRepository.count()).thenReturn(0L);
        when(checkInRepository.count()).thenReturn(0L);
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(eventRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        when(eventRepository.findByEventDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        DashboardResponse result = dashboardService.getDashboardData();

        assertEquals(0.0, result.getAttendanceRate());
    }

    @Test
    void getDashboardDataShouldIncludeCategoryDistribution() {
        Category cat1 = Category.builder().id(1L).name("Conference").build();
        Category cat2 = Category.builder().id(2L).name("Workshop").build();

        when(eventRepository.count()).thenReturn(5L);
        when(eventRepository.countByStatus(any(EventStatus.class))).thenReturn(2L);
        when(rsvpRepository.count()).thenReturn(10L);
        when(rsvpRepository.countByStatus(any(RsvpStatus.class))).thenReturn(3L);
        when(attendeeRepository.count()).thenReturn(5L);
        when(checkInRepository.count()).thenReturn(3L);
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));
        when(eventRepository.countByCategoryId(1L)).thenReturn(3L);
        when(eventRepository.countByCategoryId(2L)).thenReturn(2L);
        when(eventRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        when(eventRepository.findByEventDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        DashboardResponse result = dashboardService.getDashboardData();

        assertEquals(2, result.getEventsByCategory().size());
        assertEquals(3L, result.getEventsByCategory().get("Conference"));
        assertEquals(2L, result.getEventsByCategory().get("Workshop"));
    }

    @Test
    void getDashboardDataShouldIncludeRecentEvents() {
        User organizer = User.builder().id(1L).username("admin").fullName("Admin").build();
        Category cat = Category.builder().id(1L).name("Conference").colorCode("#FF0000").build();
        Event event = Event.builder()
                .id(1L).title("Recent Event").description("Desc")
                .eventDate(LocalDate.now()).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0))
                .location("Dublin").capacity(100).status(EventStatus.UPCOMING)
                .organizer(organizer).category(cat).build();

        when(eventRepository.count()).thenReturn(1L);
        when(eventRepository.countByStatus(any(EventStatus.class))).thenReturn(1L);
        when(rsvpRepository.count()).thenReturn(0L);
        when(rsvpRepository.countByStatus(any(RsvpStatus.class))).thenReturn(0L);
        when(rsvpRepository.countByEventIdAndStatus(anyLong(), any())).thenReturn(0L);
        when(attendeeRepository.count()).thenReturn(0L);
        when(checkInRepository.count()).thenReturn(0L);
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(eventRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of(event));
        when(eventRepository.findByEventDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        DashboardResponse result = dashboardService.getDashboardData();

        assertEquals(1, result.getRecentEvents().size());
        assertEquals("Recent Event", result.getRecentEvents().get(0).getTitle());
    }
}
