package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.AttendeeDTO;
import com.eventmanager.eventrsvp.dto.AttendeeResponse;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.Attendee;
import com.eventmanager.eventrsvp.repository.AttendeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendeeServiceTest {

    @Mock
    private AttendeeRepository attendeeRepository;

    @InjectMocks
    private AttendeeService attendeeService;

    private Attendee attendee;

    @BeforeEach
    void setUp() {
        attendee = Attendee.builder()
                .id(1L).firstName("Alice").lastName("Brown")
                .email("alice@example.com").phone("111222333")
                .organization("Test Corp")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllAttendeesShouldReturnList() {
        when(attendeeRepository.findAll()).thenReturn(List.of(attendee));
        List<AttendeeResponse> result = attendeeService.getAllAttendees();
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getFirstName());
    }

    @Test
    void getAttendeeByIdShouldReturnAttendee() {
        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        AttendeeResponse result = attendeeService.getAttendeeById(1L);
        assertEquals("Alice", result.getFirstName());
    }

    @Test
    void getAttendeeByIdShouldThrowWhenNotFound() {
        when(attendeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendeeService.getAttendeeById(99L));
    }

    @Test
    void searchAttendeesShouldReturnResults() {
        when(attendeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("alice", "alice"))
                .thenReturn(List.of(attendee));
        List<AttendeeResponse> result = attendeeService.searchAttendees("alice");
        assertEquals(1, result.size());
    }

    @Test
    void createAttendeeShouldReturnCreated() {
        AttendeeDTO dto = AttendeeDTO.builder()
                .firstName("Bob").lastName("Green")
                .email("bob@example.com").phone("444555666")
                .organization("NewCorp").build();

        when(attendeeRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(attendeeRepository.save(any(Attendee.class))).thenReturn(attendee);

        AttendeeResponse result = attendeeService.createAttendee(dto);
        assertNotNull(result);
    }

    @Test
    void createAttendeeShouldThrowOnDuplicateEmail() {
        AttendeeDTO dto = AttendeeDTO.builder()
                .firstName("Bob").lastName("Green")
                .email("alice@example.com").build();

        when(attendeeRepository.existsByEmail("alice@example.com")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> attendeeService.createAttendee(dto));
    }

    @Test
    void updateAttendeeShouldReturnUpdated() {
        AttendeeDTO dto = AttendeeDTO.builder()
                .firstName("Alice").lastName("White")
                .email("alice@example.com").phone("999888777")
                .organization("Updated Corp").build();

        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        when(attendeeRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(attendee));
        when(attendeeRepository.save(any(Attendee.class))).thenReturn(attendee);

        AttendeeResponse result = attendeeService.updateAttendee(1L, dto);
        assertNotNull(result);
    }

    @Test
    void updateAttendeeShouldThrowOnConflictingEmail() {
        Attendee otherAttendee = Attendee.builder().id(2L).email("other@example.com").build();

        AttendeeDTO dto = AttendeeDTO.builder()
                .firstName("Alice").lastName("White")
                .email("other@example.com").build();

        when(attendeeRepository.findById(1L)).thenReturn(Optional.of(attendee));
        when(attendeeRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherAttendee));

        assertThrows(BadRequestException.class, () -> attendeeService.updateAttendee(1L, dto));
    }

    @Test
    void updateAttendeeShouldThrowWhenNotFound() {
        AttendeeDTO dto = AttendeeDTO.builder().firstName("X").lastName("Y").email("x@y.com").build();
        when(attendeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendeeService.updateAttendee(99L, dto));
    }

    @Test
    void deleteAttendeeShouldSucceed() {
        when(attendeeRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> attendeeService.deleteAttendee(1L));
        verify(attendeeRepository).deleteById(1L);
    }

    @Test
    void deleteAttendeeShouldThrowWhenNotFound() {
        when(attendeeRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> attendeeService.deleteAttendee(99L));
    }
}
