package com.eventmanager.eventrsvp.model;

/**
 * Enum representing the lifecycle status of an event.
 * Events progress from UPCOMING to ONGOING to COMPLETED, or can be CANCELLED.
 */
public enum EventStatus {
    UPCOMING,
    ONGOING,
    COMPLETED,
    CANCELLED
}
