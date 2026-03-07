package com.eventmanager.eventrsvp.dto;

import com.eventmanager.eventrsvp.model.RsvpStatus;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating and updating RSVP records.
 * Links an attendee to an event with a response status and optional preferences.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RsvpDTO {

    /** ID of the event being responded to */
    @NotNull(message = "Event ID is required")
    private Long eventId;

    /** ID of the attendee submitting the RSVP */
    @NotNull(message = "Attendee ID is required")
    private Long attendeeId;

    /** RSVP status: CONFIRMED, DECLINED, MAYBE, or WAITLISTED */
    @NotNull(message = "RSVP status is required")
    private RsvpStatus status;

    /** Optional dietary preferences for event catering */
    @Size(max = 500, message = "Dietary preferences must not exceed 500 characters")
    private String dietaryPreferences;

    /** Optional special requirements like accessibility needs */
    @Size(max = 500, message = "Special requirements must not exceed 500 characters")
    private String specialRequirements;
}
