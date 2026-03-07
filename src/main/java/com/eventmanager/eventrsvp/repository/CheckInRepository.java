package com.eventmanager.eventrsvp.repository;

import com.eventmanager.eventrsvp.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CheckIn entity providing CRUD operations
 * and custom queries for attendance tracking and analytics.
 */
@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    /** Find all check-ins for a specific event */
    List<CheckIn> findByEventId(Long eventId);

    /** Find all check-ins for a specific attendee across events */
    List<CheckIn> findByAttendeeId(Long attendeeId);

    /** Find a check-in for a specific event-attendee combination */
    Optional<CheckIn> findByEventIdAndAttendeeId(Long eventId, Long attendeeId);

    /** Check if an attendee has already checked into a specific event */
    boolean existsByEventIdAndAttendeeId(Long eventId, Long attendeeId);

    /** Count total check-ins for a specific event (actual attendance) */
    long countByEventId(Long eventId);
}
