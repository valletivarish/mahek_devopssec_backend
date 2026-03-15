package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.RsvpDTO;
import com.eventmanager.eventrsvp.dto.RsvpResponse;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.*;
import com.eventmanager.eventrsvp.repository.AttendeeRepository;
import com.eventmanager.eventrsvp.repository.EventRepository;
import com.eventmanager.eventrsvp.repository.RsvpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RsvpServiceTest {

    @Mock
    private RsvpRepository rsvpRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private AttendeeRepository attendeeRepository;

    @InjectMocks
    private RsvpService rsvpService;

    private Event event;
    private Attendee attendee;
    private Rsvp rsvp;

    @BeforeEach
    void setUp() {
        User organizer = User.builder().id(1L).username("admin").fullName("Admin User").build();
        Category category = Category.builder().id(1L).name("Conference").colorCode("#FF0000").build();

        event = Event.builder()
                .id(1L).title("Test Event").capacity(100)
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0))
                .location("Dublin").status(EventStatus.UPCOMING)
                .organizer(organizer).category(category)
                .build();

        attendee = Attendee.builder()
                .id(1L).firstName("John").lastName("Doe")
                .email("john@example.com").phone("123456")
                .build();

        rsvp = Rsvp.builder()
                .id(1L).event(event).attendee(attendee)
                .status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("None").specialRequirements("None")
                .respondedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllRsvpsShouldReturnList() {
        when(rsvpRepository.findAll()).thenReturn(List.of(rsvp));
        List<RsvpResponse> result = rsvpService.getAllRsvps();
        assertEquals(1, result.size());
        assertEquals("CONFIRMED", result.get(0).getStatus().name());
    }

    @Test
    void getRsvpByIdShouldReturnRsvp() {
        when(rsvpRepository.findById(1L)).thenReturn(Optional.of(rsvp));
        RsvpResponse result = rsvpService.getRsvpById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getRsvpByIdShouldThrowWhenNotFound() {
        when(rsvpRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> rsvpService.getRsvpById(99L));
    }

    @Test
    void getRsvpsByEventShouldReturnList() {
        when(rsvpRepository.findByEventId(1L)).thenReturn(List.of(rsvp));
        List<RsvpResponse> result = rsvpService.getRsvpsByEvent(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getRsvpsByAttendeeShouldReturnList() {
        when(rsvpRepository.findByAttendeeId(1L)).thenReturn(List.of(rsvp));
        List<RsvpResponse> result = rsvpService.getRsvpsByAttendee(1L);
        assertEquals(1, result.size());
    }

    @Test
    void createRsvpShouldReturnCreated() {
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(1L).attendeeId(1L).status(RsvpStatus.CONFIRMED).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        when(rsvpRepository.findByEventIdAndAttendeeId(1L, 1L)).thenReturn(Optional.empty());
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.CONFIRMED)).thenReturn(10L);
        when(rsvpRepository.save(any(Rsvp.class))).thenReturn(rsvp);

        RsvpResponse result = rsvpService.createRsvp(dto);
        assertNotNull(result);
        assertEquals(RsvpStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void createRsvpShouldWaitlistWhenAtCapacity() {
        event.setCapacity(5);
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(1L).attendeeId(1L).status(RsvpStatus.CONFIRMED).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        when(rsvpRepository.findByEventIdAndAttendeeId(1L, 1L)).thenReturn(Optional.empty());
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.CONFIRMED)).thenReturn(5L);

        Rsvp waitlistedRsvp = Rsvp.builder()
                .id(2L).event(event).attendee(attendee).status(RsvpStatus.WAITLISTED)
                .respondedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(rsvpRepository.save(any(Rsvp.class))).thenReturn(waitlistedRsvp);

        RsvpResponse result = rsvpService.createRsvp(dto);
        assertEquals(RsvpStatus.WAITLISTED, result.getStatus());
    }

    @Test
    void createRsvpShouldThrowOnDuplicate() {
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(1L).attendeeId(1L).status(RsvpStatus.CONFIRMED).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        when(rsvpRepository.findByEventIdAndAttendeeId(1L, 1L)).thenReturn(Optional.of(rsvp));

        assertThrows(BadRequestException.class, () -> rsvpService.createRsvp(dto));
    }

    @Test
    void createRsvpShouldThrowWhenEventNotFound() {
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(99L).attendeeId(1L).status(RsvpStatus.CONFIRMED).build();
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> rsvpService.createRsvp(dto));
    }

    @Test
    void createRsvpShouldThrowWhenAttendeeNotFound() {
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(1L).attendeeId(99L).status(RsvpStatus.CONFIRMED).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> rsvpService.createRsvp(dto));
    }

    @Test
    void updateRsvpShouldReturnUpdated() {
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(1L).attendeeId(1L).status(RsvpStatus.DECLINED).build();

        when(rsvpRepository.findById(1L)).thenReturn(Optional.of(rsvp));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));

        Rsvp updatedRsvp = Rsvp.builder()
                .id(1L).event(event).attendee(attendee).status(RsvpStatus.DECLINED)
                .respondedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(rsvpRepository.save(any(Rsvp.class))).thenReturn(updatedRsvp);

        RsvpResponse result = rsvpService.updateRsvp(1L, dto);
        assertEquals(RsvpStatus.DECLINED, result.getStatus());
    }

    @Test
    void updateRsvpShouldWaitlistWhenAtCapacity() {
        event.setCapacity(5);
        rsvp.setStatus(RsvpStatus.MAYBE);

        RsvpDTO dto = RsvpDTO.builder()
                .eventId(1L).attendeeId(1L).status(RsvpStatus.CONFIRMED).build();

        when(rsvpRepository.findById(1L)).thenReturn(Optional.of(rsvp));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.CONFIRMED)).thenReturn(5L);

        Rsvp waitlistedRsvp = Rsvp.builder()
                .id(1L).event(event).attendee(attendee).status(RsvpStatus.WAITLISTED)
                .respondedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(rsvpRepository.save(any(Rsvp.class))).thenReturn(waitlistedRsvp);

        RsvpResponse result = rsvpService.updateRsvp(1L, dto);
        assertEquals(RsvpStatus.WAITLISTED, result.getStatus());
    }

    @Test
    void updateRsvpShouldNotWaitlistWhenAlreadyConfirmed() {
        event.setCapacity(5);
        rsvp.setStatus(RsvpStatus.CONFIRMED);

        RsvpDTO dto = RsvpDTO.builder()
                .eventId(1L).attendeeId(1L).status(RsvpStatus.CONFIRMED).build();

        when(rsvpRepository.findById(1L)).thenReturn(Optional.of(rsvp));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.CONFIRMED)).thenReturn(5L);
        when(rsvpRepository.save(any(Rsvp.class))).thenReturn(rsvp);

        RsvpResponse result = rsvpService.updateRsvp(1L, dto);
        assertEquals(RsvpStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void deleteRsvpShouldSucceed() {
        when(rsvpRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> rsvpService.deleteRsvp(1L));
        verify(rsvpRepository).deleteById(1L);
    }

    @Test
    void deleteRsvpShouldThrowWhenNotFound() {
        when(rsvpRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> rsvpService.deleteRsvp(99L));
    }
}
