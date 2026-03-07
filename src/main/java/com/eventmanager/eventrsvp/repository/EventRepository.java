package com.eventmanager.eventrsvp.repository;

import com.eventmanager.eventrsvp.model.Event;
import com.eventmanager.eventrsvp.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Event entity providing CRUD operations
 * and custom queries for filtering, dashboard statistics, and forecasting.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /** Find events by their status (UPCOMING, ONGOING, COMPLETED, CANCELLED) */
    List<Event> findByStatus(EventStatus status);

    /** Find events by category ID for category-based filtering */
    List<Event> findByCategoryId(Long categoryId);

    /** Find events by organiser ID to show a user's own events */
    List<Event> findByOrganizerId(Long organizerId);

    /** Find events on a specific date */
    List<Event> findByEventDate(LocalDate eventDate);

    /** Find events within a date range for calendar and trend views */
    List<Event> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    /** Count events by status for dashboard summary cards */
    long countByStatus(EventStatus status);

    /** Count events by category ID for the category distribution chart */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /** Find the 5 most recently created events for the dashboard feed */
    List<Event> findTop5ByOrderByCreatedAtDesc();

    /** Search events by title containing the search term (case-insensitive) */
    List<Event> findByTitleContainingIgnoreCase(String title);
}
