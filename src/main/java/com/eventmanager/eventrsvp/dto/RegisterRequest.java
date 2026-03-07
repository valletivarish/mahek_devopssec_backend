package com.eventmanager.eventrsvp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for new user registration. Validates that all required fields are present
 * and conform to format requirements (email format, password length, etc.).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /** Username must be 3-50 characters long */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /** Email must be in valid format */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /** Password must be at least 6 characters for security */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /** Full name displayed in the application */
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
}
