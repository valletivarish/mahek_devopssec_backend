package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.RsvpDTO;
import com.eventmanager.eventrsvp.dto.RsvpResponse;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.Attendee;
import com.eventmanager.eventrsvp.model.Event;
import com.eventmanager.eventrsvp.model.Rsvp;
import com.eventmanager.eventrsvp.model.RsvpStatus;
import com.eventmanager.eventrsvp.repository.AttendeeRepository;
import com.eventmanager.eventrsvp.repository.EventRepository;
import com.eventmanager.eventrsvp.repository.RsvpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing RSVP (Respondez s'il vous plait) records.
 *
 * RSVPs link attendees to events with a response status and optional preferences.
 * The system enforces several important business rules:
 *
 * 1. One RSVP per attendee per event: An attendee cannot submit multiple RSVPs
 *    for the same event. The database enforces this with a unique constraint on
 *    (event_id, attendee_id), and this service validates it before attempting to persist.
 *
 * 2. Automatic waitlisting: When an attendee tries to RSVP with CONFIRMED status
 *    but the event has already reached its capacity (confirmed RSVPs >= event capacity),
 *    the RSVP status is automatically downgraded to WAITLISTED. This prevents
 *    over-booking while still recording the attendee's interest.
 *
 * 3. Referential integrity: Both the event and attendee must exist in the database
 *    before an RSVP can be created. This is validated at the service level in addition
 *    to database foreign key constraints.
 */
@Service
public class RsvpService {

    /** Repository for RSVP CRUD operations and status-based queries */
    private final RsvpRepository rsvpRepository;

    /** Repository for validating event references and checking capacity */
    private final EventRepository eventRepository;

    /** Repository for validating attendee references */
    private final AttendeeRepository attendeeRepository;

    /**
     * Constructor injection of all required dependencies.
     *
     * @param rsvpRepository     repository for RSVP data access
     * @param eventRepository    repository for event validation and capacity checks
     * @param attendeeRepository repository for attendee validation
     */
    public RsvpService(RsvpRepository rsvpRepository,
                       EventRepository eventRepository,
                       AttendeeRepository attendeeRepository) {
        this.rsvpRepository = rsvpRepository;
        this.eventRepository = eventRepository;
        this.attendeeRepository = attendeeRepository;
    }

    /**
     * Retrieves all RSVPs from the database and converts them to response DTOs.
     *
     * Each response includes resolved event and attendee names for display purposes,
     * avoiding the need for the frontend to make additional API calls.
     *
     * @return a list of all RSVPs as RsvpResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<RsvpResponse> getAllRsvps() {
        return rsvpRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single RSVP by its unique identifier.
     *
     * @param id the unique identifier of the RSVP to retrieve
     * @return the RSVP data as a RsvpResponse DTO
     * @throws ResourceNotFoundException if no RSVP is found with the given ID
     */
    @Transactional(readOnly = true)
    public RsvpResponse getRsvpById(Long id) {
        Rsvp rsvp = rsvpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RSVP", id));
        return mapToResponse(rsvp);
    }

    /**
     * Retrieves all RSVPs associated with a specific event.
     *
     * This method is used to display the attendee list for an event, showing
     * who has confirmed, declined, is uncertain, or is waitlisted.
     *
     * @param eventId the ID of the event to filter RSVPs by
     * @return a list of RSVPs for the specified event
     */
    @Transactional(readOnly = true)
    public List<RsvpResponse> getRsvpsByEvent(Long eventId) {
        return rsvpRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all RSVPs submitted by a specific attendee across all events.
     *
     * This method supports viewing an attendee's event history and current
     * commitments across the platform.
     *
     * @param attendeeId the ID of the attendee to filter RSVPs by
     * @return a list of RSVPs submitted by the specified attendee
     */
    @Transactional(readOnly = true)
    public List<RsvpResponse> getRsvpsByAttendee(Long attendeeId) {
        return rsvpRepository.findByAttendeeId(attendeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new RSVP linking an attendee to an event.
     *
     * This method enforces the following business rules:
     *
     * 1. Event validation: The referenced event must exist. If not, a
     *    ResourceNotFoundException is thrown.
     *
     * 2. Attendee validation: The referenced attendee must exist. If not, a
     *    ResourceNotFoundException is thrown.
     *
     * 3. Duplicate prevention: An attendee cannot have more than one RSVP for
     *    the same event. If a duplicate is detected, a BadRequestException is thrown.
     *    The attendee should update their existing RSVP instead.
     *
     * 4. Automatic waitlisting: If the attendee requests CONFIRMED status but the
     *    event has already reached its capacity (confirmed count >= event capacity),
     *    the status is automatically changed to WAITLISTED. This prevents over-booking
     *    while preserving the attendee's place in the waiting list. The attendee will
     *    be notified (by the frontend or notification service) of their waitlisted status.
     *
     * @param rsvpDTO the DTO containing the RSVP details (event ID, attendee ID, status, preferences)
     * @return the created RSVP as a RsvpResponse DTO
     * @throws ResourceNotFoundException if the event or attendee does not exist
     * @throws BadRequestException       if the attendee already has an RSVP for the event
     */
    @Transactional
    public RsvpResponse createRsvp(RsvpDTO rsvpDTO) {
        // Validate that the referenced event exists in the database
        Event event = eventRepository.findById(rsvpDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", rsvpDTO.getEventId()));

        // Validate that the referenced attendee exists in the database
        Attendee attendee = attendeeRepository.findById(rsvpDTO.getAttendeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendee", rsvpDTO.getAttendeeId()));

        // Check for duplicate RSVP: each attendee can only have one RSVP per event.
        // If they want to change their response, they should use the update endpoint.
        rsvpRepository.findByEventIdAndAttendeeId(rsvpDTO.getEventId(), rsvpDTO.getAttendeeId())
                .ifPresent(existing -> {
                    throw new BadRequestException(
                            "Attendee already has an RSVP for this event. Use the update endpoint to modify it.");
                });

        // Determine the final RSVP status, applying the auto-waitlisting business rule.
        // If the attendee wants to confirm but the event is full, they are waitlisted instead.
        RsvpStatus finalStatus = rsvpDTO.getStatus();
        if (finalStatus == RsvpStatus.CONFIRMED) {
            long confirmedCount = rsvpRepository.countByEventIdAndStatus(
                    event.getId(), RsvpStatus.CONFIRMED);
            // If confirmed attendees have reached the event's maximum capacity,
            // automatically move this RSVP to the waitlist to prevent over-booking
            if (confirmedCount >= event.getCapacity()) {
                finalStatus = RsvpStatus.WAITLISTED;
            }
        }

        // Build the RSVP entity with the validated references and determined status
        Rsvp rsvp = Rsvp.builder()
                .event(event)
                .attendee(attendee)
                .status(finalStatus)
                .dietaryPreferences(rsvpDTO.getDietaryPreferences())
                .specialRequirements(rsvpDTO.getSpecialRequirements())
                .build();

        // Persist the new RSVP record
        Rsvp savedRsvp = rsvpRepository.save(rsvp);

        return mapToResponse(savedRsvp);
    }

    /**
     * Updates an existing RSVP with new status and/or preferences.
     *
     * The auto-waitlisting rule is also applied during updates: if the attendee
     * changes their status to CONFIRMED but the event is already at capacity
     * (excluding their own current RSVP if it was already confirmed), the status
     * is set to WAITLISTED.
     *
     * This method also re-validates event and attendee references in case the
     * DTO contains changed IDs (though typically only status and preferences change).
     *
     * @param id      the unique identifier of the RSVP to update
     * @param rsvpDTO the DTO containing the updated RSVP data
     * @return the updated RSVP as a RsvpResponse DTO
     * @throws ResourceNotFoundException if the RSVP, event, or attendee is not found
     */
    @Transactional
    public RsvpResponse updateRsvp(Long id, RsvpDTO rsvpDTO) {
        // Retrieve the existing RSVP record, throwing 404 if not found
        Rsvp rsvp = rsvpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RSVP", id));

        // Validate that the referenced event still exists
        Event event = eventRepository.findById(rsvpDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", rsvpDTO.getEventId()));

        // Validate that the referenced attendee still exists
        Attendee attendee = attendeeRepository.findById(rsvpDTO.getAttendeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendee", rsvpDTO.getAttendeeId()));

        // Apply the auto-waitlisting rule for status changes to CONFIRMED.
        // Count existing confirmed RSVPs and compare against event capacity.
        RsvpStatus finalStatus = rsvpDTO.getStatus();
        if (finalStatus == RsvpStatus.CONFIRMED) {
            long confirmedCount = rsvpRepository.countByEventIdAndStatus(
                    event.getId(), RsvpStatus.CONFIRMED);

            // If the RSVP was already confirmed, it is already counted in confirmedCount,
            // so we should not count it again when checking capacity.
            // Only apply waitlisting if this is a new confirmation that would exceed capacity.
            boolean wasAlreadyConfirmed = rsvp.getStatus() == RsvpStatus.CONFIRMED;
            long effectiveCount = wasAlreadyConfirmed ? confirmedCount - 1 : confirmedCount;

            if (effectiveCount >= event.getCapacity()) {
                finalStatus = RsvpStatus.WAITLISTED;
            }
        }

        // Apply the updated fields to the existing RSVP entity
        rsvp.setEvent(event);
        rsvp.setAttendee(attendee);
        rsvp.setStatus(finalStatus);
        rsvp.setDietaryPreferences(rsvpDTO.getDietaryPreferences());
        rsvp.setSpecialRequirements(rsvpDTO.getSpecialRequirements());

        // Persist the updated RSVP record
        Rsvp updatedRsvp = rsvpRepository.save(rsvp);

        return mapToResponse(updatedRsvp);
    }

    /**
     * Deletes an RSVP by its unique identifier.
     *
     * Removing an RSVP frees up a confirmed slot if the RSVP had CONFIRMED status,
     * potentially allowing a waitlisted attendee to be promoted. This promotion
     * logic is not handled here but could be implemented as an event listener or
     * scheduled task.
     *
     * @param id the unique identifier of the RSVP to delete
     * @throws ResourceNotFoundException if no RSVP is found with the given ID
     */
    @Transactional
    public void deleteRsvp(Long id) {
        // Verify the RSVP exists before attempting deletion
        if (!rsvpRepository.existsById(id)) {
            throw new ResourceNotFoundException("RSVP", id);
        }

        rsvpRepository.deleteById(id);
    }

    /**
     * Maps an Rsvp entity to an RsvpResponse DTO using the builder pattern.
     *
     * The response includes resolved event and attendee details (title, name, email)
     * so the frontend can display human-readable information without additional
     * API calls. The attendee name is composed by concatenating first and last names.
     *
     * @param rsvp the Rsvp entity to convert
     * @return the corresponding RsvpResponse DTO with resolved references
     */
    private RsvpResponse mapToResponse(Rsvp rsvp) {
        return RsvpResponse.builder()
                .id(rsvp.getId())
                .eventId(rsvp.getEvent().getId())
                .eventTitle(rsvp.getEvent().getTitle())
                .attendeeId(rsvp.getAttendee().getId())
                .attendeeName(rsvp.getAttendee().getFirstName() + " " + rsvp.getAttendee().getLastName())
                .attendeeEmail(rsvp.getAttendee().getEmail())
                .status(rsvp.getStatus())
                .dietaryPreferences(rsvp.getDietaryPreferences())
                .specialRequirements(rsvp.getSpecialRequirements())
                .respondedAt(rsvp.getRespondedAt())
                .updatedAt(rsvp.getUpdatedAt())
                .build();
    }
}
