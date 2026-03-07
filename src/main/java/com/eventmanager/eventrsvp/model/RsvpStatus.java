package com.eventmanager.eventrsvp.model;

/**
 * Enum representing the RSVP response status for an attendee.
 * CONFIRMED means attending, DECLINED means not attending,
 * MAYBE means uncertain, and WAITLISTED means capacity was full.
 */
public enum RsvpStatus {
    CONFIRMED,
    DECLINED,
    MAYBE,
    WAITLISTED
}
