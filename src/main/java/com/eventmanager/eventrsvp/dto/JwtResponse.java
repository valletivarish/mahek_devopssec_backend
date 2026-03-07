package com.eventmanager.eventrsvp.dto;

import lombok.*;

/**
 * DTO returned after successful authentication containing the JWT token
 * and basic user information for the frontend to store in context.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    /** JWT access token for authenticating subsequent API requests */
    private String token;

    /** Token type, always "Bearer" for JWT-based auth */
    private String type;

    /** Authenticated user's unique ID */
    private Long id;

    /** Authenticated user's username */
    private String username;

    /** Authenticated user's email */
    private String email;

    /** Authenticated user's role (ADMIN or USER) */
    private String role;
}
