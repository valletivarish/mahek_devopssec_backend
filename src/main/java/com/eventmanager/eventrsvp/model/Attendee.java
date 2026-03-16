package com.eventmanager.eventrsvp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a person who can RSVP to events and be checked in.
 * Attendees are separate from application users - they represent real-world people
 * who attend events, identified by their unique email address.
 */
@Entity
@Table(name = "attendees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** First name of the attendee */
    @Column(nullable = false, length = 100)
    private String firstName;

    /** Last name of the attendee */
    @Column(nullable = false, length = 100)
    private String lastName;

    /** Unique email address used for identification and notifications */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Linked user account (null for externally added attendees) */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    /** Contact phone number in international format */
    @Column(length = 20)
    private String phone;

    /** Organisation or company the attendee belongs to */
    @Column(length = 200)
    private String organization;

    /** Timestamp when the attendee record was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the attendee record was last updated */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
