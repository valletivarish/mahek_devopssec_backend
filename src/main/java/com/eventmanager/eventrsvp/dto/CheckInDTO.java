package com.eventmanager.eventrsvp.dto;

import com.eventmanager.eventrsvp.model.CheckInMethod;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating check-in records when attendees arrive at an event.
 * Validates that event and attendee IDs are provided along with the check-in method.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInDTO {

    /** ID of the event where the check-in is happening */
    @NotNull(message = "Event ID is required")
    private Long eventId;

    /** ID of the attendee being checked in */
    @NotNull(message = "Attendee ID is required")
    private Long attendeeId;

    /** Method of check-in: QR_CODE or MANUAL */
    @NotNull(message = "Check-in method is required")
    private CheckInMethod checkInMethod;

    /** Optional notes from event staff about the check-in */
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
