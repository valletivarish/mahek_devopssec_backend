package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.EventDTO;
import com.eventmanager.eventrsvp.dto.EventResponse;
import com.eventmanager.eventrsvp.model.EventStatus;
import com.eventmanager.eventrsvp.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing events.
 * Events are the core entity of the system, representing scheduled gatherings
 * that attendees can RSVP to and check into. Supports full CRUD operations
 * along with filtering by status, category, and title search.
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    /**
     * Constructor injection of EventService which encapsulates
     * all business logic for event lifecycle management.
     */
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Retrieves all events in the system.
     * Used by the main events listing page to display all available events.
     *
     * @return 200 OK with a list of all event records including RSVP counts
     */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves a single event by its unique identifier.
     * Used when viewing the event detail page, which shows full information
     * including description, schedule, location, and RSVP statistics.
     *
     * @param id the unique identifier of the event to retrieve
     * @return 200 OK with the event data, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable("id") Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    /**
     * Retrieves all events that match the given lifecycle status.
     * Allows the frontend to filter events by UPCOMING, ONGOING, COMPLETED, or CANCELLED.
     *
     * @param status the EventStatus enum value to filter by
     * @return 200 OK with a list of events matching the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponse>> getEventsByStatus(@PathVariable("status") EventStatus status) {
        List<EventResponse> events = eventService.getEventsByStatus(status);
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves all events belonging to a specific category.
     * Supports the category-based filtering feature on the events listing page.
     *
     * @param categoryId the unique identifier of the category to filter by
     * @return 200 OK with a list of events in the specified category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<EventResponse>> getEventsByCategory(@PathVariable("categoryId") Long categoryId) {
        List<EventResponse> events = eventService.getEventsByCategory(categoryId);
        return ResponseEntity.ok(events);
    }

    /**
     * Searches for events whose title contains the given query string.
     * Performs a case-insensitive partial match to support the search bar
     * and typeahead functionality on the frontend.
     *
     * @param title the search term to match against event titles
     * @return 200 OK with a list of events whose titles match the query
     */
    @GetMapping("/search")
    public ResponseEntity<List<EventResponse>> searchEvents(@RequestParam("title") String title) {
        List<EventResponse> events = eventService.searchEvents(title);
        return ResponseEntity.ok(events);
    }

    /**
     * Creates a new event in the system.
     * Extracts the authenticated user's username from the SecurityContext
     * to set as the event organiser. Validates that the event date is not
     * in the past and that the category exists.
     *
     * @param eventDTO validated payload containing title, description, date, time, location, capacity, and category
     * @return 201 Created with the newly created event data including its generated ID
     */
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        EventResponse created = eventService.createEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing event's details.
     * Allows modification of all event fields including title, schedule,
     * location, capacity, status, and category assignment.
     *
     * @param id       the unique identifier of the event to update
     * @param eventDTO validated payload with the updated event fields
     * @return 200 OK with the updated event data, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable("id") Long id,
                                                     @Valid @RequestBody EventDTO eventDTO) {
        EventResponse updated = eventService.updateEvent(id, eventDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an event by its unique identifier.
     * Associated RSVPs and check-ins will also be removed based on cascade rules.
     *
     * @param id the unique identifier of the event to delete
     * @return 204 No Content on successful deletion, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
