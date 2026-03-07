package com.eventmanager.eventrsvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 6 configuration using the SecurityFilterChain approach.
 * Configures stateless JWT-based authentication, CORS, CSRF (disabled for REST API),
 * and endpoint authorization rules. Public endpoints include auth, Swagger UI,
 * health checks, and GET requests for read-only access.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /** Constructor injection of the JWT filter */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Configures the security filter chain with JWT authentication,
     * stateless sessions, and endpoint-level access control.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF since we use stateless JWT tokens instead of sessions
            .csrf(csrf -> csrf.disable())
            // Use stateless session management for REST API
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow authentication endpoints without login
                .requestMatchers("/api/auth/**").permitAll()
                // Allow Swagger UI and OpenAPI docs
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                        "/swagger-ui.html").permitAll()
                // Allow health check endpoint for CI/CD smoke tests
                .requestMatchers("/api/health").permitAll()
                // Allow GET requests for read-only access to resources
                .requestMatchers(HttpMethod.GET, "/api/events/**",
                        "/api/categories/**", "/api/attendees/**",
                        "/api/rsvps/**", "/api/checkins/**",
                        "/api/dashboard/**", "/api/forecast/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            // Add JWT filter before the default username/password authentication filter
            .addFilterBefore(jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder for hashing user passwords securely.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager bean required for programmatic authentication
     * in the auth controller during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
