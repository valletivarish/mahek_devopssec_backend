package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.EventDTO;
import com.eventmanager.eventrsvp.dto.EventResponse;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.Category;
import com.eventmanager.eventrsvp.model.Event;
import com.eventmanager.eventrsvp.model.EventStatus;
import com.eventmanager.eventrsvp.model.RsvpStatus;
import com.eventmanager.eventrsvp.model.User;
import com.eventmanager.eventrsvp.repository.CategoryRepository;
import com.eventmanager.eventrsvp.repository.EventRepository;
import com.eventmanager.eventrsvp.repository.RsvpRepository;
import com.eventmanager.eventrsvp.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing events in the RSVP and Attendance system.
 *
 * Events are the central entity of the application. Each event belongs to a category,
 * is organised by a user, and can receive RSVPs and check-ins from attendees.
 *
 * This service provides full CRUD operations along with filtering and search capabilities:
 * - Filter events by their lifecycle status (UPCOMING, ONGOING, COMPLETED, CANCELLED)
 * - Filter events by their category for topic-based browsing
 * - Search events by title with case-insensitive partial matching
 *
 * When building EventResponse DTOs, the service enriches each response with the
 * confirmedCount (number of confirmed RSVPs) by querying the RsvpRepository.
 * This count is essential for capacity management and dashboard displays.
 *
 * The organiser is determined from the Spring Security context during event creation,
 * ensuring that events are always attributed to the authenticated user.
 */
@Service
public class EventService {

    /** Repository for event CRUD operations and custom queries */
    private final EventRepository eventRepository;

    /** Repository for validating category references when creating/updating events */
    private final CategoryRepository categoryRepository;

    /** Repository for resolving the authenticated user as the event organiser */
    private final UserRepository userRepository;

    /** Repository for querying confirmed RSVP counts per event */
    private final RsvpRepository rsvpRepository;

    /**
     * Constructor injection of all required dependencies.
     *
     * @param eventRepository    repository for event data access
     * @param categoryRepository repository for category validation
     * @param userRepository     repository for user/organiser lookup
     * @param rsvpRepository     repository for RSVP count queries
     */
    public EventService(EventRepository eventRepository,
                        CategoryRepository categoryRepository,
                        UserRepository userRepository,
                        RsvpRepository rsvpRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.rsvpRepository = rsvpRepository;
    }

    /**
     * Retrieves all events from the database with their confirmed RSVP counts.
     *
     * Each event in the response includes the confirmedCount field, which represents
     * the number of attendees who have confirmed their attendance. This is calculated
     * by querying the RSVP table for entries with CONFIRMED status for each event.
     *
     * @return a list of all events as EventResponse DTOs with confirmed counts
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single event by its unique identifier with confirmed RSVP count.
     *
     * @param id the unique identifier of the event to retrieve
     * @return the event data as an EventResponse DTO with confirmed count
     * @throws ResourceNotFoundException if no event is found with the given ID
     */
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        return mapToResponse(event);
    }

    /**
     * Retrieves all events that match the given lifecycle status.
     *
     * This method supports filtering the event list by status, allowing the frontend
     * to show separate tabs or views for upcoming, ongoing, completed, and cancelled events.
     *
     * @param status the event status to filter by (UPCOMING, ONGOING, COMPLETED, CANCELLED)
     * @return a list of matching events as EventResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all events belonging to the specified category.
     *
     * This method supports category-based filtering, allowing users to browse
     * events by topic (e.g., all Conferences, all Workshops).
     *
     * @param categoryId the ID of the category to filter events by
     * @return a list of matching events as EventResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByCategory(Long categoryId) {
        return eventRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Searches for events whose title contains the given query string.
     *
     * The search is case-insensitive and matches partial titles, supporting
     * the search bar functionality in the frontend event listing page.
     *
     * @param title the search string to match against event titles
     * @return a list of matching events as EventResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<EventResponse> searchEvents(String title) {
        return eventRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new event associated with the currently authenticated user as organiser.
     *
     * The creation process follows these steps:
     * 1. Validates that the referenced category exists in the database.
     *    Events must belong to a valid category for proper classification.
     * 2. Resolves the currently authenticated user from the Spring Security context
     *    to set them as the event organiser. This ensures that events are always
     *    attributed to a real, authenticated user.
     * 3. Builds the Event entity with all provided fields and default status (UPCOMING).
     * 4. Persists the event and returns the response with a confirmed count of 0
     *    (since a newly created event has no RSVPs yet).
     *
     * @param eventDTO the DTO containing the event details
     * @return the created event as an EventResponse DTO
     * @throws ResourceNotFoundException if the specified category does not exist
     * @throws BadRequestException       if the authenticated user cannot be resolved
     */
    @Transactional
    public EventResponse createEvent(EventDTO eventDTO) {
        // Validate that the specified category exists in the database.
        // Events must reference a valid category for proper classification and filtering.
        Category category = categoryRepository.findById(eventDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", eventDTO.getCategoryId()));

        // Resolve the currently authenticated user to set as the event organiser.
        // The username is extracted from the Spring Security context, which was
        // populated during JWT token authentication.
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException(
                        "Authenticated user '" + username + "' not found in the database"));

        // Build the Event entity from the DTO fields with the resolved category and organiser.
        // The status defaults to UPCOMING if not explicitly provided in the DTO.
        Event event = Event.builder()
                .title(eventDTO.getTitle())
                .description(eventDTO.getDescription())
                .eventDate(eventDTO.getEventDate())
                .startTime(eventDTO.getStartTime())
                .endTime(eventDTO.getEndTime())
                .location(eventDTO.getLocation())
                .capacity(eventDTO.getCapacity())
                .status(eventDTO.getStatus() != null ? eventDTO.getStatus() : EventStatus.UPCOMING)
                .organizer(organizer)
                .category(category)
                .build();

        // Persist the new event to the database
        Event savedEvent = eventRepository.save(event);

        return mapToResponse(savedEvent);
    }

    /**
     * Updates an existing event with new data.
     *
     * The update process validates that:
     * 1. The event with the given ID exists in the database
     * 2. The referenced category (if changed) exists in the database
     *
     * All mutable fields are updated from the DTO. The organiser and creation
     * timestamp remain unchanged, as they are set once during creation.
     *
     * @param id       the unique identifier of the event to update
     * @param eventDTO the DTO containing the updated event data
     * @return the updated event as an EventResponse DTO
     * @throws ResourceNotFoundException if the event or category is not found
     */
    @Transactional
    public EventResponse updateEvent(Long id, EventDTO eventDTO) {
        // Retrieve the existing event, throwing 404 if not found
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));

        // Validate that the referenced category exists
        Category category = categoryRepository.findById(eventDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", eventDTO.getCategoryId()));

        // Apply updated fields to the existing event entity.
        // The organiser remains unchanged as event ownership does not transfer.
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setEventDate(eventDTO.getEventDate());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setLocation(eventDTO.getLocation());
        event.setCapacity(eventDTO.getCapacity());
        event.setCategory(category);

        // Only update status if explicitly provided in the DTO.
        // This prevents accidentally resetting the status when only other fields are modified.
        if (eventDTO.getStatus() != null) {
            event.setStatus(eventDTO.getStatus());
        }

        // Persist the updated event
        Event updatedEvent = eventRepository.save(event);

        return mapToResponse(updatedEvent);
    }

    /**
     * Deletes an event by its unique identifier.
     *
     * Cascading deletions of related RSVPs and check-ins depend on the database
     * foreign key configuration. If cascading is not set up, related records
     * must be deleted first to avoid constraint violations.
     *
     * @param id the unique identifier of the event to delete
     * @throws ResourceNotFoundException if no event is found with the given ID
     */
    @Transactional
    public void deleteEvent(Long id) {
        // Verify the event exists before attempting deletion
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }

        eventRepository.deleteById(id);
    }

    /**
     * Maps an Event entity to an EventResponse DTO, enriched with the confirmed RSVP count.
     *
     * The confirmedCount is fetched from the RsvpRepository by counting all RSVP records
     * for the event that have a CONFIRMED status. This count is used by the frontend
     * to display availability (e.g., "25/100 spots filled") and by the RSVP service
     * to determine whether new RSVPs should be auto-waitlisted.
     *
     * The organiser and category names are resolved from the lazy-loaded relationships
     * on the Event entity, avoiding additional queries thanks to the open session
     * provided by the @Transactional annotation.
     *
     * @param event the Event entity to convert
     * @return the corresponding EventResponse DTO with confirmed count
     */
    private EventResponse mapToResponse(Event event) {
        // Query the confirmed RSVP count for this event to include in the response.
        // This count is critical for capacity management and UI display.
        long confirmedCount = rsvpRepository.countByEventIdAndStatus(
                event.getId(), RsvpStatus.CONFIRMED);

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .capacity(event.getCapacity())
                .status(event.getStatus())
                .organizerId(event.getOrganizer().getId())
                .organizerName(event.getOrganizer().getFullName())
                .categoryId(event.getCategory().getId())
                .categoryName(event.getCategory().getName())
                .categoryColorCode(event.getCategory().getColorCode())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .confirmedCount(confirmedCount)
                .build();
    }
}
