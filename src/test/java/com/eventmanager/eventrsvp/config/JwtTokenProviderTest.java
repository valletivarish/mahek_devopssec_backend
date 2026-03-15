package com.eventmanager.eventrsvp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "TestSecretKeyForJWTTokenGenerationInTestEnvironment2024",
                86400000L
        );
    }

    @Test
    void generateTokenShouldReturnNonEmptyString() {
        String token = jwtTokenProvider.generateToken("testuser");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUsernameFromTokenShouldReturnCorrectUsername() {
        String token = jwtTokenProvider.generateToken("admin");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("admin", username);
    }

    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        String token = jwtTokenProvider.generateToken("testuser");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateTokenShouldReturnFalseForInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateTokenShouldReturnFalseForEmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void validateTokenShouldReturnFalseForNull() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void differentUsernamesShouldProduceDifferentTokens() {
        String token1 = jwtTokenProvider.generateToken("user1");
        String token2 = jwtTokenProvider.generateToken("user2");
        assertNotEquals(token1, token2);
    }
}
