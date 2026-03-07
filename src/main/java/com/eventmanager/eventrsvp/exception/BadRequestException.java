package com.eventmanager.eventrsvp.exception;

/**
 * Custom exception thrown when a client request contains invalid data
 * or violates business rules. Results in a 400 HTTP response.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
