package com.eventmanager.eventrsvp.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility component for JWT token operations: generation, validation, and parsing.
 * Uses HMAC-SHA256 signing with a configurable secret key and expiration time.
 * The secret key and expiration are loaded from application.properties.
 */
@Component
public class JwtTokenProvider {

    /** Secret key used for signing JWT tokens, loaded from configuration */
    private final SecretKey secretKey;

    /** Token expiration time in milliseconds */
    private final long jwtExpiration;

    /**
     * Constructor injection of JWT configuration values.
     * Generates a secure HMAC-SHA key from the configured secret string.
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration}") long jwtExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpiration = jwtExpiration;
    }

    /**
     * Generates a JWT token for the given username.
     * The token contains the username as the subject and has an expiration time.
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts the username (subject) from a JWT token.
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates whether a JWT token is well-formed and not expired.
     * Returns false for any parsing or validation errors.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
