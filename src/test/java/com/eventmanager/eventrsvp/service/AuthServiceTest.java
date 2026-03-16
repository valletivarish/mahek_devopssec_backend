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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AttendeeRepository attendeeRepository;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .password("encodedPassword").fullName("Test User")
                .role(UserRole.USER).build();
    }

    @Test
    void registerShouldReturnJwtResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFullName("New User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        Attendee attendee = Attendee.builder().id(1L).firstName("Test").lastName("User").email("test@example.com").build();
        when(attendeeRepository.save(any(Attendee.class))).thenReturn(attendee);
        when(jwtTokenProvider.generateToken("testuser")).thenReturn("jwt-token");

        JwtResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("Bearer", result.getType());
    }

    @Test
    void registerShouldThrowOnDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFullName("Test");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void registerShouldThrowOnDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void loginShouldReturnJwtResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(attendeeRepository.findByUserId(1L)).thenReturn(Optional.of(
                Attendee.builder().id(1L).firstName("Test").lastName("User").build()));
        when(jwtTokenProvider.generateToken("testuser")).thenReturn("jwt-token");

        JwtResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("testuser", result.getUsername());
    }
}
