package com.eventmanager.eventrsvp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Health check controller providing a simple endpoint for CI/CD pipeline
 * smoke tests and load balancer health checks to verify the application is running.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /** Returns a 200 OK response with status UP when the application is healthy */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "application", "Event RSVP Manager"));
    }

    /** Returns application version and environment info for deployment verification */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> appInfo() {
        return ResponseEntity.ok(Map.of(
                "application", "Event RSVP and Attendance Manager",
                "version", "1.0.0",
                "environment", System.getProperty("spring.profiles.active", "default"),
                "java", System.getProperty("java.version")
        ));
    }
}
