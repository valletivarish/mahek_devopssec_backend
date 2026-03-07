package com.eventmanager.eventrsvp.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO for returning attendee data to the frontend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String organization;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
