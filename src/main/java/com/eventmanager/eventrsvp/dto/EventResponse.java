package com.eventmanager.eventrsvp.dto;

import com.eventmanager.eventrsvp.model.EventStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Response DTO for returning event data to the frontend.
 * Includes resolved organiser and category names instead of just IDs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private Integer capacity;
    private EventStatus status;
    private Long organizerId;
    private String organizerName;
    private Long categoryId;
    private String categoryName;
    private String categoryColorCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** Count of confirmed RSVPs for this event */
    private Long confirmedCount;
}
