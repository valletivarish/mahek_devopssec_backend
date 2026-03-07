package com.eventmanager.eventrsvp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing an RSVP response from an attendee for a specific event.
 * Tracks the attendee's response status, dietary preferences, and special requirements.
 * A unique constraint ensures one RSVP per attendee per event.
 */
@Entity
@Table(name = "rsvps", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "attendee_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rsvp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The event this RSVP is for */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** The attendee who submitted this RSVP */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", nullable = false)
    private Attendee attendee;

    /** Current RSVP status: CONFIRMED, DECLINED, MAYBE, or WAITLISTED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RsvpStatus status;

    /** Dietary preferences or restrictions for catering (e.g., vegetarian, gluten-free) */
    @Column(length = 500)
    private String dietaryPreferences;

    /** Any special requirements such as accessibility needs */
    @Column(length = 500)
    private String specialRequirements;

    /** Timestamp when the RSVP was first submitted */
    @Column(nullable = false, updatable = false)
    private LocalDateTime respondedAt;

    /** Timestamp when the RSVP was last modified */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.respondedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
