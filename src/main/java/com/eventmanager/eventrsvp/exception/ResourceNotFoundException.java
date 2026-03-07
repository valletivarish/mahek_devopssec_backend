package com.eventmanager.eventrsvp.exception;

/**
 * Custom exception thrown when a requested resource (event, attendee, RSVP, etc.)
 * is not found in the database. Results in a 404 HTTP response.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with ID: " + id);
    }
}
