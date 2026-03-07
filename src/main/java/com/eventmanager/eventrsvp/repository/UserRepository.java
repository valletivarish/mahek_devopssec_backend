package com.eventmanager.eventrsvp.repository;

import com.eventmanager.eventrsvp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for User entity providing CRUD operations
 * and custom query methods for authentication lookups.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find a user by their username for login authentication */
    Optional<User> findByUsername(String username);

    /** Check if a username already exists to prevent duplicate registrations */
    boolean existsByUsername(String username);

    /** Check if an email already exists to prevent duplicate registrations */
    boolean existsByEmail(String email);
}
