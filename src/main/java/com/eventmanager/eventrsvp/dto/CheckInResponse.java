package com.eventmanager.eventrsvp.dto;

import com.eventmanager.eventrsvp.model.CheckInMethod;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO for returning check-in data with resolved event and attendee names.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInResponse {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long attendeeId;
    private String attendeeName;
    private String attendeeEmail;
    private LocalDateTime checkInTime;
    private CheckInMethod checkInMethod;
    private String notes;
}
