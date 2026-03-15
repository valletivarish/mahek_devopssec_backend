package com.eventmanager.eventrsvp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a check-in record when an attendee arrives at an event.
 * Each check-in is linked to both the event and the attendee, and records the
 * method used (QR code scan or manual entry) along with optional notes.
 * A unique constraint prevents duplicate check-ins for the same attendee at the same event.
 */
@Entity
@Table(name = "check_ins", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "attendee_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The event where the check-in occurred */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** The attendee who checked in */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attendee_id", nullable = false)
    private Attendee attendee;

    /** Exact timestamp when the attendee checked in */
    @Column(nullable = false)
    private LocalDateTime checkInTime;

    /** Method used for check-in: QR_CODE or MANUAL */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckInMethod checkInMethod;

    /** Optional notes added by event staff during check-in */
    @Column(length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (this.checkInTime == null) {
            this.checkInTime = LocalDateTime.now();
        }
    }
}
