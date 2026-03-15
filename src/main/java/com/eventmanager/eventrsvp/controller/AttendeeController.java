package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.AttendeeDTO;
import com.eventmanager.eventrsvp.dto.AttendeeResponse;
import com.eventmanager.eventrsvp.service.AttendeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing attendee records.
 * Attendees represent individuals who can RSVP to events and be checked in.
 * Provides full CRUD operations plus a search capability for finding
 * attendees by name, email, or organisation.
 */
@RestController
@RequestMapping("/api/attendees")
@CrossOrigin(origins = "*")
public class AttendeeController {

    private final AttendeeService attendeeService;

    /**
     * Constructor injection of AttendeeService which encapsulates
     * all business logic for attendee management.
     */
    public AttendeeController(AttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }

    /**
     * Retrieves all attendees registered in the system.
     * Used by the admin panel to display the complete attendee list.
     *
     * @return 200 OK with a list of all attendee records
     */
    @GetMapping
    public ResponseEntity<List<AttendeeResponse>> getAllAttendees() {
        List<AttendeeResponse> attendees = attendeeService.getAllAttendees();
        return ResponseEntity.ok(attendees);
    }

    /**
     * Retrieves a single attendee by their unique identifier.
     * Used when viewing an attendee's profile or editing their details.
     *
     * @param id the unique identifier of the attendee to retrieve
     * @return 200 OK with the attendee data, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttendeeResponse> getAttendeeById(@PathVariable("id") Long id) {
        AttendeeResponse attendee = attendeeService.getAttendeeById(id);
        return ResponseEntity.ok(attendee);
    }

    /**
     * Searches for attendees matching the given query string.
     * The search is performed against first name, last name, email, and organisation
     * fields using a case-insensitive partial match to support typeahead functionality.
     *
     * @param query the search term to match against attendee fields
     * @return 200 OK with a list of matching attendee records
     */
    @GetMapping("/search")
    public ResponseEntity<List<AttendeeResponse>> searchAttendees(@RequestParam("query") String query) {
        List<AttendeeResponse> results = attendeeService.searchAttendees(query);
        return ResponseEntity.ok(results);
    }

    /**
     * Creates a new attendee record in the system.
     * Validates that the email address is unique before persisting.
     *
     * @param attendeeDTO validated payload containing first name, last name, email, phone, and organisation
     * @return 201 Created with the newly created attendee data including its generated ID
     */
    @PostMapping
    public ResponseEntity<AttendeeResponse> createAttendee(@Valid @RequestBody AttendeeDTO attendeeDTO) {
        AttendeeResponse created = attendeeService.createAttendee(attendeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing attendee's details.
     * Allows modification of all attendee fields except the unique ID.
     *
     * @param id          the unique identifier of the attendee to update
     * @param attendeeDTO validated payload with the updated attendee fields
     * @return 200 OK with the updated attendee data, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<AttendeeResponse> updateAttendee(@PathVariable("id") Long id,
                                                           @Valid @RequestBody AttendeeDTO attendeeDTO) {
        AttendeeResponse updated = attendeeService.updateAttendee(id, attendeeDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an attendee by their unique identifier.
     * Associated RSVPs and check-ins may also be removed depending on cascade rules.
     *
     * @param id the unique identifier of the attendee to delete
     * @return 204 No Content on successful deletion, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendee(@PathVariable("id") Long id) {
        attendeeService.deleteAttendee(id);
        return ResponseEntity.noContent().build();
    }
}
