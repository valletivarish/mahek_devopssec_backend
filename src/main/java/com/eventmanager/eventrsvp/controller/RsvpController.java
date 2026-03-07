package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.RsvpDTO;
import com.eventmanager.eventrsvp.dto.RsvpResponse;
import com.eventmanager.eventrsvp.service.RsvpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing RSVP (Respondez S'il Vous Plait) records.
 * RSVPs link attendees to events with a response status (CONFIRMED, DECLINED,
 * MAYBE, WAITLISTED) and optional preferences. Supports full CRUD operations
 * plus filtering by event and attendee.
 */
@RestController
@RequestMapping("/api/rsvps")
@CrossOrigin(origins = "*")
public class RsvpController {

    private final RsvpService rsvpService;

    /**
     * Constructor injection of RsvpService which encapsulates
     * all business logic for RSVP management including capacity checks.
     */
    public RsvpController(RsvpService rsvpService) {
        this.rsvpService = rsvpService;
    }

    /**
     * Retrieves all RSVPs across all events.
     * Primarily used by administrators to view the complete list of responses.
     *
     * @return 200 OK with a list of all RSVP records including resolved event and attendee names
     */
    @GetMapping
    public ResponseEntity<List<RsvpResponse>> getAllRsvps() {
        List<RsvpResponse> rsvps = rsvpService.getAllRsvps();
        return ResponseEntity.ok(rsvps);
    }

    /**
     * Retrieves a single RSVP by its unique identifier.
     * Used when viewing or editing a specific RSVP's details and preferences.
     *
     * @param id the unique identifier of the RSVP to retrieve
     * @return 200 OK with the RSVP data, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<RsvpResponse> getRsvpById(@PathVariable Long id) {
        RsvpResponse rsvp = rsvpService.getRsvpById(id);
        return ResponseEntity.ok(rsvp);
    }

    /**
     * Retrieves all RSVPs for a specific event.
     * Used on the event detail page to display the attendee response list
     * and calculate RSVP statistics (confirmed, declined, maybe counts).
     *
     * @param eventId the unique identifier of the event whose RSVPs to retrieve
     * @return 200 OK with a list of RSVP records for the specified event
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<RsvpResponse>> getRsvpsByEvent(@PathVariable Long eventId) {
        List<RsvpResponse> rsvps = rsvpService.getRsvpsByEvent(eventId);
        return ResponseEntity.ok(rsvps);
    }

    /**
     * Retrieves all RSVPs submitted by a specific attendee.
     * Used on the attendee profile page to show all events the attendee
     * has responded to and their current status for each.
     *
     * @param attendeeId the unique identifier of the attendee whose RSVPs to retrieve
     * @return 200 OK with a list of RSVP records for the specified attendee
     */
    @GetMapping("/attendee/{attendeeId}")
    public ResponseEntity<List<RsvpResponse>> getRsvpsByAttendee(@PathVariable Long attendeeId) {
        List<RsvpResponse> rsvps = rsvpService.getRsvpsByAttendee(attendeeId);
        return ResponseEntity.ok(rsvps);
    }

    /**
     * Creates a new RSVP linking an attendee to an event.
     * Validates that the event and attendee exist, that no duplicate RSVP exists,
     * and checks event capacity before allowing a CONFIRMED status.
     * If the event is at capacity, the RSVP may be automatically set to WAITLISTED.
     *
     * @param rsvpDTO validated payload containing event ID, attendee ID, status, and optional preferences
     * @return 201 Created with the newly created RSVP data including its generated ID
     */
    @PostMapping
    public ResponseEntity<RsvpResponse> createRsvp(@Valid @RequestBody RsvpDTO rsvpDTO) {
        RsvpResponse created = rsvpService.createRsvp(rsvpDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing RSVP's status or preferences.
     * Allows attendees to change their response (e.g., from MAYBE to CONFIRMED)
     * or update dietary preferences and special requirements.
     *
     * @param id      the unique identifier of the RSVP to update
     * @param rsvpDTO validated payload with the updated RSVP fields
     * @return 200 OK with the updated RSVP data, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<RsvpResponse> updateRsvp(@PathVariable Long id,
                                                   @Valid @RequestBody RsvpDTO rsvpDTO) {
        RsvpResponse updated = rsvpService.updateRsvp(id, rsvpDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an RSVP by its unique identifier.
     * Removes the attendee's response to the event, freeing up capacity
     * if the RSVP was in CONFIRMED status.
     *
     * @param id the unique identifier of the RSVP to delete
     * @return 204 No Content on successful deletion, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRsvp(@PathVariable Long id) {
        rsvpService.deleteRsvp(id);
        return ResponseEntity.noContent().build();
    }
}
