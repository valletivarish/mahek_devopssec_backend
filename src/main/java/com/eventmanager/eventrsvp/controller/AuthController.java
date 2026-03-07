package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.JwtResponse;
import com.eventmanager.eventrsvp.dto.LoginRequest;
import com.eventmanager.eventrsvp.dto.RegisterRequest;
import com.eventmanager.eventrsvp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller handling user registration and login.
 * Issues JWT tokens upon successful authentication that the frontend
 * stores and sends with subsequent API requests for authorisation.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor injection of the AuthService for handling
     * registration logic and credential validation.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account in the system.
     * Validates that the username and email are unique, hashes the password,
     * persists the user record, and returns a JWT token for immediate login.
     *
     * @param registerRequest validated registration payload containing username, email, password, and full name
     * @return 201 Created with a JwtResponse containing the token and user details
     */
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        JwtResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates an existing user with username and password.
     * Validates the credentials against the stored bcrypt hash and,
     * if successful, generates and returns a JWT token.
     *
     * @param loginRequest validated login payload containing username and password
     * @return 200 OK with a JwtResponse containing the token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
