package com.eventmanager.eventrsvp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing an event category (e.g., Conference, Workshop, Social).
 * Categories help organise and filter events. Each category has a unique name
 * and an optional colour code for visual distinction in the UI.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique category name such as Conference, Workshop, Seminar */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Optional description explaining the category purpose */
    @Column(length = 500)
    private String description;

    /** Hex colour code for UI display (e.g., #FF5733) */
    @Column(length = 7)
    private String colorCode;

    /** Timestamp when this category was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
