package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.CheckInDTO;
import com.eventmanager.eventrsvp.dto.CheckInResponse;
import com.eventmanager.eventrsvp.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing event check-in records.
 * Check-ins track actual attendee arrivals at events, recording the time
 * and method of check-in (QR code scan or manual entry by event staff).
 * This data feeds into attendance analytics and the forecasting model.
 */
@RestController
@RequestMapping("/api/checkins")
@CrossOrigin(origins = "*")
public class CheckInController {

    private final CheckInService checkInService;

    /**
     * Constructor injection of CheckInService which encapsulates
     * all business logic for check-in operations and duplicate prevention.
     */
    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    /**
     * Retrieves all check-in records across all events.
     * Used by administrators to view the complete check-in log
     * and audit attendance across the platform.
     *
     * @return 200 OK with a list of all check-in records including resolved event and attendee names
     */
    @GetMapping
    public ResponseEntity<List<CheckInResponse>> getAllCheckIns() {
        List<CheckInResponse> checkIns = checkInService.getAllCheckIns();
        return ResponseEntity.ok(checkIns);
    }

    /**
     * Retrieves a single check-in record by its unique identifier.
     * Used when viewing the details of a specific check-in entry.
     *
     * @param id the unique identifier of the check-in to retrieve
     * @return 200 OK with the check-in data, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CheckInResponse> getCheckInById(@PathVariable("id") Long id) {
        CheckInResponse checkIn = checkInService.getCheckInById(id);
        return ResponseEntity.ok(checkIn);
    }

    /**
     * Retrieves all check-in records for a specific event.
     * Provides real-time visibility into who has arrived at the event,
     * enabling organisers to track attendance progress against RSVP counts.
     *
     * @param eventId the unique identifier of the event whose check-ins to retrieve
     * @return 200 OK with a list of check-in records for the specified event
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<CheckInResponse>> getCheckInsByEvent(@PathVariable("eventId") Long eventId) {
        List<CheckInResponse> checkIns = checkInService.getCheckInsByEvent(eventId);
        return ResponseEntity.ok(checkIns);
    }

    /**
     * Creates a new check-in record when an attendee arrives at an event.
     * Validates that the event and attendee exist, that the attendee has
     * a confirmed RSVP, and that they have not already been checked in.
     * Records the timestamp and check-in method (QR_CODE or MANUAL).
     *
     * @param checkInDTO validated payload containing event ID, attendee ID, check-in method, and optional notes
     * @return 201 Created with the newly created check-in record
     */
    @PostMapping
    public ResponseEntity<CheckInResponse> createCheckIn(@Valid @RequestBody CheckInDTO checkInDTO) {
        CheckInResponse created = checkInService.createCheckIn(checkInDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Deletes a check-in record by its unique identifier.
     * Used to correct erroneous check-ins (e.g., accidental duplicate scans).
     *
     * @param id the unique identifier of the check-in to delete
     * @return 204 No Content on successful deletion, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheckIn(@PathVariable("id") Long id) {
        checkInService.deleteCheckIn(id);
        return ResponseEntity.noContent().build();
    }
}
