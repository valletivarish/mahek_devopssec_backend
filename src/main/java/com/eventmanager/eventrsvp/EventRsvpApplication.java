package com.eventmanager.eventrsvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Event RSVP and Attendance Manager application.
 * This Spring Boot application manages events, RSVPs, attendees, categories,
 * and check-ins with JWT-based authentication and attendance forecasting.
 */
@SpringBootApplication
public class EventRsvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventRsvpApplication.class, args);
    }
}
