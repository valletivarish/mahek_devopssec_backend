package com.eventmanager.eventrsvp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing an application user who can organise events and manage RSVPs.
 * Stores authentication credentials (hashed password) and profile information.
 * The email field is unique to prevent duplicate registrations.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique username used for login */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Unique email address for the user */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** BCrypt-hashed password for secure authentication */
    @Column(nullable = false)
    private String password;

    /** Full display name of the user */
    @Column(nullable = false, length = 100)
    private String fullName;

    /** Role determines access level: ADMIN or USER */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /** Timestamp when the user account was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = UserRole.USER;
        }
    }
}
