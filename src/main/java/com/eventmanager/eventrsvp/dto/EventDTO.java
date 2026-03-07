package com.eventmanager.eventrsvp.dto;

import com.eventmanager.eventrsvp.model.EventStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for creating and updating events. Validates that title, date, time,
 * location, and capacity conform to business rules before persisting.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTO {

    /** Event title, required and limited to 200 characters */
    @NotBlank(message = "Event title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    /** Optional detailed description of the event */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /** Date of the event, must not be in the past */
    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    /** Start time of the event */
    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    /** End time of the event */
    @NotNull(message = "End time is required")
    private LocalTime endTime;

    /** Location where the event will be held */
    @NotBlank(message = "Location is required")
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    /** Maximum attendee capacity, must be between 1 and 10000 */
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity must not exceed 10000")
    private Integer capacity;

    /** Event status: UPCOMING, ONGOING, COMPLETED, or CANCELLED */
    private EventStatus status;

    /** ID of the category this event belongs to */
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
