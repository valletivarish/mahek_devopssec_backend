package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.AttendeeDTO;
import com.eventmanager.eventrsvp.dto.AttendeeResponse;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.Attendee;
import com.eventmanager.eventrsvp.repository.AttendeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing attendee records.
 *
 * Attendees represent real-world people who attend events. They are distinct from
 * application Users (who are organisers and administrators). Each attendee is
 * identified by a unique email address, which serves as the primary deduplication key.
 *
 * This service provides full CRUD operations and a name-based search feature for
 * autocomplete functionality in the frontend. The search queries both first and
 * last names in a case-insensitive manner.
 *
 * Business rules enforced:
 * - Email addresses must be unique across all attendees
 * - When updating, the uniqueness check excludes the attendee being updated
 *   to allow keeping the same email while preventing conflicts with others
 */
@Service
public class AttendeeService {

    /** Repository for accessing and persisting attendee data */
    private final AttendeeRepository attendeeRepository;

    /**
     * Constructor injection of the AttendeeRepository dependency.
     *
     * @param attendeeRepository the repository for attendee CRUD operations
     */
    public AttendeeService(AttendeeRepository attendeeRepository) {
        this.attendeeRepository = attendeeRepository;
    }

    /**
     * Retrieves all attendees from the database and converts them to response DTOs.
     *
     * Returns a complete list of all registered attendees. In a production system
     * with large datasets, this should be replaced with paginated queries.
     *
     * @return a list of all attendees as AttendeeResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<AttendeeResponse> getAllAttendees() {
        return attendeeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single attendee by their unique identifier.
     *
     * @param id the unique identifier of the attendee to retrieve
     * @return the attendee data as an AttendeeResponse DTO
     * @throws ResourceNotFoundException if no attendee is found with the given ID
     */
    @Transactional(readOnly = true)
    public AttendeeResponse getAttendeeById(Long id) {
        Attendee attendee = attendeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee", id));
        return mapToResponse(attendee);
    }

    /**
     * Searches for attendees whose first or last name contains the given query string.
     *
     * This method supports the autocomplete / search functionality in the frontend,
     * allowing event organisers to quickly find attendees when creating RSVPs or
     * performing check-ins. The search is case-insensitive and matches partial names.
     *
     * The same query string is used to search both first name and last name fields,
     * so searching for "john" will match "John Smith" (first name match) and
     * "Mary Johnson" (last name match).
     *
     * @param query the search string to match against attendee names
     * @return a list of matching attendees as AttendeeResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<AttendeeResponse> searchAttendees(String query) {
        // Search both first and last name fields with the same query term.
        // The repository method uses Spring Data JPA's method name query derivation
        // to generate a case-insensitive LIKE query on both columns with OR logic.
        return attendeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new attendee record after validating email uniqueness.
     *
     * Each attendee must have a unique email address to prevent duplicate records
     * in the system. The email serves as the natural identifier for attendees
     * and is used for communication and deduplication.
     *
     * The createdAt and updatedAt timestamps are automatically set by the
     * @PrePersist callback on the Attendee entity.
     *
     * @param attendeeDTO the DTO containing the new attendee's personal information
     * @return the created attendee as an AttendeeResponse DTO
     * @throws BadRequestException if an attendee with the same email already exists
     */
    @Transactional
    public AttendeeResponse createAttendee(AttendeeDTO attendeeDTO) {
        // Enforce unique email constraint to prevent duplicate attendee records
        if (attendeeRepository.existsByEmail(attendeeDTO.getEmail())) {
            throw new BadRequestException(
                    "Attendee with email '" + attendeeDTO.getEmail() + "' already exists");
        }

        // Build the Attendee entity from the validated DTO fields
        Attendee attendee = Attendee.builder()
                .firstName(attendeeDTO.getFirstName())
                .lastName(attendeeDTO.getLastName())
                .email(attendeeDTO.getEmail())
                .phone(attendeeDTO.getPhone())
                .organization(attendeeDTO.getOrganization())
                .build();

        // Persist the new attendee record
        Attendee savedAttendee = attendeeRepository.save(attendee);

        return mapToResponse(savedAttendee);
    }

    /**
     * Updates an existing attendee record with new data.
     *
     * The email uniqueness check excludes the attendee being updated. This allows
     * the attendee to keep their current email address without triggering a false
     * duplicate error, while still preventing them from changing to an email that
     * belongs to another attendee.
     *
     * The updatedAt timestamp is automatically refreshed by the @PreUpdate callback
     * on the Attendee entity.
     *
     * @param id          the unique identifier of the attendee to update
     * @param attendeeDTO the DTO containing the updated attendee information
     * @return the updated attendee as an AttendeeResponse DTO
     * @throws ResourceNotFoundException if no attendee is found with the given ID
     * @throws BadRequestException       if the new email conflicts with another attendee
     */
    @Transactional
    public AttendeeResponse updateAttendee(Long id, AttendeeDTO attendeeDTO) {
        // Retrieve the existing attendee, throwing 404 if not found
        Attendee attendee = attendeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee", id));

        // Validate email uniqueness excluding the current attendee.
        // This prevents the attendee from accidentally taking another attendee's email
        // while allowing them to keep their own email unchanged.
        attendeeRepository.findByEmail(attendeeDTO.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException(
                                "Attendee with email '" + attendeeDTO.getEmail() + "' already exists");
                    }
                });

        // Apply the updated fields to the existing entity
        attendee.setFirstName(attendeeDTO.getFirstName());
        attendee.setLastName(attendeeDTO.getLastName());
        attendee.setEmail(attendeeDTO.getEmail());
        attendee.setPhone(attendeeDTO.getPhone());
        attendee.setOrganization(attendeeDTO.getOrganization());

        // Persist the updated attendee record
        Attendee updatedAttendee = attendeeRepository.save(attendee);

        return mapToResponse(updatedAttendee);
    }

    /**
     * Deletes an attendee record by its unique identifier.
     *
     * Note: If RSVPs or check-ins reference this attendee, the database foreign key
     * constraints will prevent deletion. The caller should handle or cascade deletions
     * of related records before removing the attendee.
     *
     * @param id the unique identifier of the attendee to delete
     * @throws ResourceNotFoundException if no attendee is found with the given ID
     */
    @Transactional
    public void deleteAttendee(Long id) {
        // Verify the attendee exists before attempting deletion
        if (!attendeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendee", id);
        }

        attendeeRepository.deleteById(id);
    }

    /**
     * Maps an Attendee entity to an AttendeeResponse DTO using the builder pattern.
     *
     * This private helper centralises the entity-to-DTO transformation to maintain
     * consistency across all service methods and avoid code duplication.
     *
     * @param attendee the Attendee entity to convert
     * @return the corresponding AttendeeResponse DTO
     */
    private AttendeeResponse mapToResponse(Attendee attendee) {
        return AttendeeResponse.builder()
                .id(attendee.getId())
                .firstName(attendee.getFirstName())
                .lastName(attendee.getLastName())
                .email(attendee.getEmail())
                .phone(attendee.getPhone())
                .organization(attendee.getOrganization())
                .createdAt(attendee.getCreatedAt())
                .updatedAt(attendee.getUpdatedAt())
                .build();
    }
}
