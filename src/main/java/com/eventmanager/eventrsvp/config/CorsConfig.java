package com.eventmanager.eventrsvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration to allow the React frontend to communicate with the backend API.
 * Permits requests from localhost (development) and the S3 static hosting URL (production).
 * Allows standard HTTP methods and the Authorization header for JWT tokens.
 */
@Configuration
public class CorsConfig {

    /**
     * Creates a CORS filter allowing cross-origin requests from the frontend.
     * In production, the allowed origins should be restricted to the actual deployment URL.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow frontend origins for local development and production
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "*"
        ));

        // Allow standard HTTP methods used by the REST API
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow headers needed for JWT authentication and content negotiation
        config.setAllowedHeaders(List.of("*"));

        // Allow credentials for cookie-based auth fallback
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
