package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.EventDTO;
import com.eventmanager.eventrsvp.dto.EventResponse;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.*;
import com.eventmanager.eventrsvp.repository.CategoryRepository;
import com.eventmanager.eventrsvp.repository.EventRepository;
import com.eventmanager.eventrsvp.repository.RsvpRepository;
import com.eventmanager.eventrsvp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RsvpRepository rsvpRepository;

    @InjectMocks
    private EventService eventService;

    private Event event;
    private Category category;
    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = User.builder().id(1L).username("admin").fullName("Admin User").build();
        category = Category.builder().id(1L).name("Conference").colorCode("#FF0000").build();

        event = Event.builder()
                .id(1L).title("Test Event").description("Description")
                .capacity(100).eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0))
                .location("Dublin").status(EventStatus.UPCOMING)
                .organizer(organizer).category(category)
                .build();
    }

    @Test
    void getAllEventsShouldReturnList() {
        when(eventRepository.findAll()).thenReturn(List.of(event));
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.CONFIRMED)).thenReturn(5L);
        List<EventResponse> result = eventService.getAllEvents();
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
    }

    @Test
    void getEventByIdShouldReturnEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.CONFIRMED)).thenReturn(5L);
        EventResponse result = eventService.getEventById(1L);
        assertEquals("Test Event", result.getTitle());
    }

    @Test
    void getEventByIdShouldThrowWhenNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(99L));
    }

    @Test
    void getEventsByStatusShouldReturnFiltered() {
        when(eventRepository.findByStatus(EventStatus.UPCOMING)).thenReturn(List.of(event));
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.CONFIRMED)).thenReturn(0L);
        List<EventResponse> result = eventService.getEventsByStatus(EventStatus.UPCOMING);
        assertEquals(1, result.size());
    }

    @Test
    void getEventsByCategoryShouldReturnFiltered() {
        when(eventRepository.findByCategoryId(1L)).thenReturn(List.of(event));
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.CONFIRMED)).thenReturn(0L);
        List<EventResponse> result = eventService.getEventsByCategory(1L);
        assertEquals(1, result.size());
    }

    @Test
    void searchEventsShouldReturnResults() {
        when(eventRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(List.of(event));
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.CONFIRMED)).thenReturn(0L);
        List<EventResponse> result = eventService.searchEvents("Test");
        assertEquals(1, result.size());
    }

    @Test
    void createEventShouldReturnCreated() {
        EventDTO dto = EventDTO.builder()
                .title("New Event").description("Desc")
                .eventDate(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(16, 0))
                .location("Cork").capacity(50).categoryId(1L)
                .status(EventStatus.UPCOMING).build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        SecurityContextHolder.setContext(securityContext);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(rsvpRepository.countByEventIdAndStatus(anyLong(), any())).thenReturn(0L);

        EventResponse result = eventService.createEvent(dto);
        assertNotNull(result);
    }

    @Test
    void createEventShouldThrowWhenCategoryNotFound() {
        EventDTO dto = EventDTO.builder()
                .title("New Event").categoryId(99L).build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> eventService.createEvent(dto));
    }

    @Test
    void updateEventShouldReturnUpdated() {
        EventDTO dto = EventDTO.builder()
                .title("Updated Event").description("Updated")
                .eventDate(LocalDate.now().plusDays(20))
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(16, 0))
                .location("Galway").capacity(200).categoryId(1L)
                .status(EventStatus.ONGOING).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(rsvpRepository.countByEventIdAndStatus(anyLong(), any())).thenReturn(0L);

        EventResponse result = eventService.updateEvent(1L, dto);
        assertNotNull(result);
    }

    @Test
    void updateEventShouldThrowWhenNotFound() {
        EventDTO dto = EventDTO.builder().title("Updated").categoryId(1L).build();
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(99L, dto));
    }

    @Test
    void deleteEventShouldSucceed() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> eventService.deleteEvent(1L));
        verify(eventRepository).deleteById(1L);
    }

    @Test
    void deleteEventShouldThrowWhenNotFound() {
        when(eventRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> eventService.deleteEvent(99L));
    }
}
