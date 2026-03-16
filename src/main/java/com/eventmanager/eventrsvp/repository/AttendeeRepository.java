package com.eventmanager.eventrsvp.repository;

import com.eventmanager.eventrsvp.model.Attendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Attendee entity providing CRUD operations
 * and custom queries for lookup by email and search by name.
 */
@Repository
public interface AttendeeRepository extends JpaRepository<Attendee, Long> {

    /** Find an attendee by their unique email address */
    Optional<Attendee> findByEmail(String email);

    /** Check if an attendee with the given email already exists */
    boolean existsByEmail(String email);

    /** Search attendees by first or last name (case-insensitive) for autocomplete */
    List<Attendee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    /** Find the attendee linked to a specific user */
    Optional<Attendee> findByUserId(Long userId);
}
