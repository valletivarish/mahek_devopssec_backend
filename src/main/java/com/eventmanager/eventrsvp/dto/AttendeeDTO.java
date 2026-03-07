package com.eventmanager.eventrsvp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating and updating attendee records.
 * Validates name, email format, and field length constraints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendeeDTO {

    /** First name is required, max 100 characters */
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    /** Last name is required, max 100 characters */
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    /** Email must be valid format and unique in the system */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    /** Optional phone number, max 20 characters */
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    /** Optional organisation name */
    @Size(max = 200, message = "Organisation must not exceed 200 characters")
    private String organization;
}
