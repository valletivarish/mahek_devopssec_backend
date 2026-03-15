package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.CheckInDTO;
import com.eventmanager.eventrsvp.dto.CheckInResponse;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.*;
import com.eventmanager.eventrsvp.repository.AttendeeRepository;
import com.eventmanager.eventrsvp.repository.CheckInRepository;
import com.eventmanager.eventrsvp.repository.EventRepository;
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
class CheckInServiceTest {

    @Mock
    private CheckInRepository checkInRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private AttendeeRepository attendeeRepository;

    @InjectMocks
    private CheckInService checkInService;

    private Event event;
    private Attendee attendee;
    private CheckIn checkIn;

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
                .id(1L).firstName("Jane").lastName("Smith")
                .email("jane@example.com").phone("654321")
                .build();

        checkIn = CheckIn.builder()
                .id(1L).event(event).attendee(attendee)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(CheckInMethod.QR_CODE)
                .notes("On time")
                .build();
    }

    @Test
    void getAllCheckInsShouldReturnList() {
        when(checkInRepository.findAll()).thenReturn(List.of(checkIn));
        List<CheckInResponse> result = checkInService.getAllCheckIns();
        assertEquals(1, result.size());
        assertEquals("QR_CODE", result.get(0).getCheckInMethod().name());
    }

    @Test
    void getCheckInByIdShouldReturnCheckIn() {
        when(checkInRepository.findById(1L)).thenReturn(Optional.of(checkIn));
        CheckInResponse result = checkInService.getCheckInById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getCheckInByIdShouldThrowWhenNotFound() {
        when(checkInRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> checkInService.getCheckInById(99L));
    }

    @Test
    void getCheckInsByEventShouldReturnList() {
        when(checkInRepository.findByEventId(1L)).thenReturn(List.of(checkIn));
        List<CheckInResponse> result = checkInService.getCheckInsByEvent(1L);
        assertEquals(1, result.size());
    }

    @Test
    void createCheckInShouldReturnCreated() {
        CheckInDTO dto = CheckInDTO.builder()
                .eventId(1L).attendeeId(1L).checkInMethod(CheckInMethod.MANUAL).notes("Late arrival").build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        when(checkInRepository.existsByEventIdAndAttendeeId(1L, 1L)).thenReturn(false);
        when(checkInRepository.save(any(CheckIn.class))).thenReturn(checkIn);

        CheckInResponse result = checkInService.createCheckIn(dto);
        assertNotNull(result);
    }

    @Test
    void createCheckInShouldThrowOnDuplicate() {
        CheckInDTO dto = CheckInDTO.builder()
                .eventId(1L).attendeeId(1L).checkInMethod(CheckInMethod.QR_CODE).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        when(checkInRepository.existsByEventIdAndAttendeeId(1L, 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> checkInService.createCheckIn(dto));
    }

    @Test
    void createCheckInShouldThrowWhenEventNotFound() {
        CheckInDTO dto = CheckInDTO.builder()
                .eventId(99L).attendeeId(1L).checkInMethod(CheckInMethod.QR_CODE).build();
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> checkInService.createCheckIn(dto));
    }

    @Test
    void createCheckInShouldThrowWhenAttendeeNotFound() {
        CheckInDTO dto = CheckInDTO.builder()
                .eventId(1L).attendeeId(99L).checkInMethod(CheckInMethod.QR_CODE).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(attendeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> checkInService.createCheckIn(dto));
    }

    @Test
    void deleteCheckInShouldSucceed() {
        when(checkInRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> checkInService.deleteCheckIn(1L));
        verify(checkInRepository).deleteById(1L);
    }

    @Test
    void deleteCheckInShouldThrowWhenNotFound() {
        when(checkInRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> checkInService.deleteCheckIn(99L));
    }
}
