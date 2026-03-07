package com.eventmanager.eventrsvp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for user login requests. Contains username and password
 * that are validated against the database for JWT token generation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /** Username must not be blank for authentication */
    @NotBlank(message = "Username is required")
    private String username;

    /** Password must not be blank for authentication */
    @NotBlank(message = "Password is required")
    private String password;
}
