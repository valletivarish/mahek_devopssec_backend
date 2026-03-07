package com.eventmanager.eventrsvp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test that verifies the Spring Boot application context loads
 * successfully with all beans, configurations, and dependencies wired correctly.
 * Uses the test profile to connect to H2 in-memory database instead of MySQL.
 */
@SpringBootTest
@ActiveProfiles("test")
class EventRsvpApplicationTests {

    /**
     * Verifies that the application context starts without any errors.
     * This test catches configuration issues, missing beans, and circular dependencies.
     */
    @Test
    void contextLoads() {
        // Context loading itself is the test - if it fails, Spring Boot configuration has issues
    }
}
