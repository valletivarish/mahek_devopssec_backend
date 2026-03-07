package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.CheckInDTO;
import com.eventmanager.eventrsvp.dto.CheckInResponse;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.Attendee;
import com.eventmanager.eventrsvp.model.CheckIn;
import com.eventmanager.eventrsvp.model.Event;
import com.eventmanager.eventrsvp.repository.AttendeeRepository;
import com.eventmanager.eventrsvp.repository.CheckInRepository;
import com.eventmanager.eventrsvp.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing check-in records at events.
 *
 * Check-ins represent the physical or virtual arrival of an attendee at an event.
 * They are the primary data source for actual attendance tracking, as opposed to
 * RSVPs which represent intended attendance.
 *
 * Each check-in records:
 * - Which event the attendee arrived at
 * - Which attendee checked in
 * - The exact timestamp of arrival
 * - The method used (QR_CODE scan or MANUAL entry by staff)
 * - Optional notes from event staff
 *
 * Business rules enforced:
 * - An attendee can only check in once per event (duplicate prevention).
 *   This is enforced both at the service level and by a database unique constraint.
 * - Both the event and attendee must exist before a check-in can be recorded.
 * - The check-in timestamp defaults to the current time if not explicitly set.
 */
@Service
public class CheckInService {

    /** Repository for check-in CRUD operations and attendance queries */
    private final CheckInRepository checkInRepository;

    /** Repository for validating event references */
    private final EventRepository eventRepository;

    /** Repository for validating attendee references */
    private final AttendeeRepository attendeeRepository;

    /**
     * Constructor injection of all required dependencies.
     *
     * @param checkInRepository  repository for check-in data access
     * @param eventRepository    repository for event validation
     * @param attendeeRepository repository for attendee validation
     */
    public CheckInService(CheckInRepository checkInRepository,
                          EventRepository eventRepository,
                          AttendeeRepository attendeeRepository) {
        this.checkInRepository = checkInRepository;
        this.eventRepository = eventRepository;
        this.attendeeRepository = attendeeRepository;
    }

    /**
     * Retrieves all check-in records from the database.
     *
     * Each response includes resolved event and attendee details for display.
     * In a production system with high event volumes, this should be paginated.
     *
     * @return a list of all check-ins as CheckInResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<CheckInResponse> getAllCheckIns() {
        return checkInRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single check-in record by its unique identifier.
     *
     * @param id the unique identifier of the check-in to retrieve
     * @return the check-in data as a CheckInResponse DTO
     * @throws ResourceNotFoundException if no check-in is found with the given ID
     */
    @Transactional(readOnly = true)
    public CheckInResponse getCheckInById(Long id) {
        CheckIn checkIn = checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckIn", id));
        return mapToResponse(checkIn);
    }

    /**
     * Retrieves all check-in records for a specific event.
     *
     * This method provides the attendance list for an event, showing which attendees
     * have actually arrived. It is used for real-time attendance monitoring during
     * events and for post-event attendance reports.
     *
     * @param eventId the ID of the event to filter check-ins by
     * @return a list of check-ins for the specified event
     */
    @Transactional(readOnly = true)
    public List<CheckInResponse> getCheckInsByEvent(Long eventId) {
        return checkInRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new check-in record for an attendee at an event.
     *
     * The check-in creation process enforces several business rules:
     *
     * 1. Event validation: The referenced event must exist in the database.
     *    Check-ins cannot be recorded for non-existent events.
     *
     * 2. Attendee validation: The referenced attendee must exist in the database.
     *    Check-ins can only be recorded for registered attendees.
     *
     * 3. Duplicate prevention: An attendee cannot check in more than once at
     *    the same event. This prevents accidental double-counting of attendance
     *    from repeated QR code scans or manual entry errors. If a duplicate is
     *    detected, a BadRequestException is thrown.
     *
     * 4. Automatic timestamping: The check-in time defaults to the current timestamp
     *    via the @PrePersist callback on the CheckIn entity.
     *
     * @param checkInDTO the DTO containing the check-in details (event ID, attendee ID, method, notes)
     * @return the created check-in as a CheckInResponse DTO
     * @throws ResourceNotFoundException if the event or attendee does not exist
     * @throws BadRequestException       if the attendee has already checked into this event
     */
    @Transactional
    public CheckInResponse createCheckIn(CheckInDTO checkInDTO) {
        // Validate that the referenced event exists in the database
        Event event = eventRepository.findById(checkInDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", checkInDTO.getEventId()));

        // Validate that the referenced attendee exists in the database
        Attendee attendee = attendeeRepository.findById(checkInDTO.getAttendeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendee", checkInDTO.getAttendeeId()));

        // Prevent duplicate check-ins for the same attendee at the same event.
        // This guards against accidental double-scans of QR codes or repeated
        // manual entries by event staff, ensuring accurate attendance counts.
        if (checkInRepository.existsByEventIdAndAttendeeId(
                checkInDTO.getEventId(), checkInDTO.getAttendeeId())) {
            throw new BadRequestException(
                    "Attendee has already checked into this event");
        }

        // Build the CheckIn entity with validated references and the specified method.
        // The checkInTime is set to now by the @PrePersist callback if not provided.
        CheckIn checkIn = CheckIn.builder()
                .event(event)
                .attendee(attendee)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(checkInDTO.getCheckInMethod())
                .notes(checkInDTO.getNotes())
                .build();

        // Persist the new check-in record
        CheckIn savedCheckIn = checkInRepository.save(checkIn);

        return mapToResponse(savedCheckIn);
    }

    /**
     * Deletes a check-in record by its unique identifier.
     *
     * This operation is typically used to correct erroneous check-ins (e.g.,
     * the wrong attendee was checked in by mistake). After deletion, the
     * attendee can be checked in again if needed.
     *
     * @param id the unique identifier of the check-in to delete
     * @throws ResourceNotFoundException if no check-in is found with the given ID
     */
    @Transactional
    public void deleteCheckIn(Long id) {
        // Verify the check-in exists before attempting deletion
        if (!checkInRepository.existsById(id)) {
            throw new ResourceNotFoundException("CheckIn", id);
        }

        checkInRepository.deleteById(id);
    }

    /**
     * Maps a CheckIn entity to a CheckInResponse DTO using the builder pattern.
     *
     * The response includes resolved event title and attendee name/email for
     * human-readable display in the frontend, avoiding the need for additional
     * API calls to resolve these references.
     *
     * @param checkIn the CheckIn entity to convert
     * @return the corresponding CheckInResponse DTO with resolved references
     */
    private CheckInResponse mapToResponse(CheckIn checkIn) {
        return CheckInResponse.builder()
                .id(checkIn.getId())
                .eventId(checkIn.getEvent().getId())
                .eventTitle(checkIn.getEvent().getTitle())
                .attendeeId(checkIn.getAttendee().getId())
                .attendeeName(checkIn.getAttendee().getFirstName() + " " + checkIn.getAttendee().getLastName())
                .attendeeEmail(checkIn.getAttendee().getEmail())
                .checkInTime(checkIn.getCheckInTime())
                .checkInMethod(checkIn.getCheckInMethod())
                .notes(checkIn.getNotes())
                .build();
    }
}
