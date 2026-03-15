package com.eventmanager.eventrsvp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * JPA entity representing an event that attendees can RSVP to and check into.
 * Each event belongs to a category and is organised by a user. The capacity
 * field controls how many confirmed RSVPs are allowed before waitlisting begins.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Title of the event displayed in listings and dashboards */
    @Column(nullable = false, length = 200)
    private String title;

    /** Detailed description of the event including agenda and objectives */
    @Column(length = 2000)
    private String description;

    /** Date on which the event takes place */
    @Column(nullable = false)
    private LocalDate eventDate;

    /** Start time of the event */
    @Column(nullable = false)
    private LocalTime startTime;

    /** End time of the event */
    @Column(nullable = false)
    private LocalTime endTime;

    /** Physical or virtual location of the event */
    @Column(nullable = false, length = 500)
    private String location;

    /** Maximum number of confirmed attendees allowed (1-10000) */
    @Column(nullable = false)
    private Integer capacity;

    /** Current lifecycle status of the event */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    /** Many-to-one relationship with the user who organised the event */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    /** Many-to-one relationship with the event category */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Timestamp when the event record was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the event record was last updated */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = EventStatus.UPCOMING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
