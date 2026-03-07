package com.eventmanager.eventrsvp.repository;

import com.eventmanager.eventrsvp.model.Rsvp;
import com.eventmanager.eventrsvp.model.RsvpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Rsvp entity providing CRUD operations
 * and custom queries for event-specific RSVP statistics and filtering.
 */
@Repository
public interface RsvpRepository extends JpaRepository<Rsvp, Long> {

    /** Find all RSVPs for a specific event */
    List<Rsvp> findByEventId(Long eventId);

    /** Find all RSVPs for a specific attendee */
    List<Rsvp> findByAttendeeId(Long attendeeId);

    /** Find RSVPs for an event filtered by status */
    List<Rsvp> findByEventIdAndStatus(Long eventId, RsvpStatus status);

    /** Find an existing RSVP for a specific event-attendee combination */
    Optional<Rsvp> findByEventIdAndAttendeeId(Long eventId, Long attendeeId);

    /** Count RSVPs by status for a specific event (used for capacity checking) */
    long countByEventIdAndStatus(Long eventId, RsvpStatus status);

    /** Count total RSVPs by status across all events (for dashboard) */
    long countByStatus(RsvpStatus status);

    /** Count confirmed RSVPs for events in a specific category (for forecasting) */
    @Query("SELECT COUNT(r) FROM Rsvp r WHERE r.event.category.id = :categoryId AND r.status = 'CONFIRMED'")
    long countConfirmedByCategoryId(@Param("categoryId") Long categoryId);
}
