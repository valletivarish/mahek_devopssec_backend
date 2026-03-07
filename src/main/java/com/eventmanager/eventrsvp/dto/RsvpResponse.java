package com.eventmanager.eventrsvp.dto;

import com.eventmanager.eventrsvp.model.RsvpStatus;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO for returning RSVP data with resolved event and attendee details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RsvpResponse {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long attendeeId;
    private String attendeeName;
    private String attendeeEmail;
    private RsvpStatus status;
    private String dietaryPreferences;
    private String specialRequirements;
    private LocalDateTime respondedAt;
    private LocalDateTime updatedAt;
}
