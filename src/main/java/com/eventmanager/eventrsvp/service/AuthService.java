package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.config.JwtTokenProvider;
import com.eventmanager.eventrsvp.dto.JwtResponse;
import com.eventmanager.eventrsvp.dto.LoginRequest;
import com.eventmanager.eventrsvp.dto.RegisterRequest;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.model.Attendee;
import com.eventmanager.eventrsvp.model.User;
import com.eventmanager.eventrsvp.model.UserRole;
import com.eventmanager.eventrsvp.repository.AttendeeRepository;
import com.eventmanager.eventrsvp.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user authentication and registration operations.
 *
 * This service provides two core functions:
 * 1. User Registration: Creates new user accounts with validated, unique credentials
 *    and returns a JWT token so the user is immediately authenticated after sign-up.
 * 2. User Login: Authenticates existing users against stored credentials using
 *    Spring Security's AuthenticationManager and returns a JWT token for subsequent
 *    API calls.
 *
 * All passwords are hashed using BCrypt before storage, and JWT tokens are generated
 * using the application's configured secret key and expiration settings.
 */
@Service
public class AuthService {

    /** Repository for persisting and querying user records */
    private final UserRepository userRepository;

    /** BCrypt password encoder for hashing passwords before storage */
    private final PasswordEncoder passwordEncoder;

    /** Spring Security's authentication manager for validating login credentials */
    private final AuthenticationManager authenticationManager;

    /** Utility for generating and validating JWT tokens */
    private final JwtTokenProvider jwtTokenProvider;

    /** Repository for creating attendee records linked to users */
    private final AttendeeRepository attendeeRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       AttendeeRepository attendeeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.attendeeRepository = attendeeRepository;
    }

    /**
     * Registers a new user account in the system.
     *
     * The registration process follows these steps:
     * 1. Validates that the chosen username is not already taken by another user.
     *    Usernames must be unique to prevent identity conflicts during authentication.
     * 2. Validates that the email address is not already registered. This prevents
     *    duplicate accounts and ensures each person has a single account.
     * 3. Hashes the plaintext password using BCrypt before storing it. BCrypt
     *    automatically generates a random salt and applies multiple rounds of hashing
     *    to protect against rainbow table and brute-force attacks.
     * 4. Creates and persists the User entity with the USER role by default.
     *    Admin accounts must be created through a separate administrative process.
     * 5. Generates a JWT token for the newly created user so they are immediately
     *    logged in after registration without needing a separate login step.
     *
     * @param request the registration request containing username, email, password, and full name
     * @return a JwtResponse containing the authentication token and user details
     * @throws BadRequestException if the username or email is already registered
     */
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        // Check for username uniqueness to prevent duplicate login identities
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }

        // Check for email uniqueness to prevent duplicate accounts
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        // Build the User entity with BCrypt-hashed password and default USER role.
        // The @PrePersist callback on the User entity will set createdAt timestamp
        // and default role if not explicitly provided.
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.USER)
                .build();

        // Persist the new user to the database
        User savedUser = userRepository.save(user);

        // Auto-create a linked attendee record for non-admin users
        String[] nameParts = savedUser.getFullName().split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        Attendee attendee = attendeeRepository.save(Attendee.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(savedUser.getEmail())
                .user(savedUser)
                .build());

        String token = jwtTokenProvider.generateToken(savedUser.getUsername());

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .attendeeId(attendee.getId())
                .build();
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * The login process follows these steps:
     * 1. Delegates credential validation to Spring Security's AuthenticationManager,
     *    which internally uses the CustomUserDetailsService to load the user and
     *    the PasswordEncoder to verify the password hash. If authentication fails,
     *    Spring Security throws an AuthenticationException.
     * 2. Sets the successful authentication in the SecurityContext so it is available
     *    throughout the request lifecycle for authorisation checks.
     * 3. Retrieves the full User entity from the database to populate the response
     *    with user profile information (ID, email, role).
     * 4. Generates a JWT token containing the username as the subject claim.
     *
     * @param request the login request containing username and password
     * @return a JwtResponse containing the authentication token and user details
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        // Authenticate the user credentials using Spring Security's authentication manager.
        // This triggers the CustomUserDetailsService to load the user from the database
        // and the PasswordEncoder to verify the provided password against the stored hash.
        // An AuthenticationException is thrown if credentials are invalid.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Store the successful authentication in the security context so that
        // downstream filters and controllers can access the authenticated principal
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Retrieve the full user entity to include profile details in the response.
        // We use the username from the authentication result to ensure consistency.
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Generate a JWT token for the authenticated user
        String token = jwtTokenProvider.generateToken(user.getUsername());

        // Look up linked attendee ID for the user
        Long attendeeId = attendeeRepository.findByUserId(user.getId())
                .map(Attendee::getId)
                .orElse(null);

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .attendeeId(attendeeId)
                .build();
    }
}
