package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.model.User;
import com.eventmanager.eventrsvp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService} interface.
 * This service bridges the application's User entity with the Spring Security
 * authentication framework by loading user-specific data from the database.
 *
 * During the authentication process, Spring Security calls {@link #loadUserByUsername}
 * to retrieve user credentials and authorities. This class translates our domain
 * User model into a Spring Security {@link UserDetails} object that the framework
 * can use for authentication and authorisation decisions.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /** Repository for accessing user data from the database */
    private final UserRepository userRepository;

    /**
     * Constructor injection of the UserRepository dependency.
     * Spring automatically injects the repository bean at application startup.
     *
     * @param userRepository the repository used to query user records
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user from the database by their username and converts the domain User
     * entity into a Spring Security {@link UserDetails} object.
     *
     * This method is called by Spring Security's authentication manager during
     * the login process. It performs the following steps:
     * 1. Queries the database for a user with the given username
     * 2. Throws an exception if no user is found (triggering authentication failure)
     * 3. Maps the user's role (e.g., ADMIN, USER) to a Spring Security GrantedAuthority
     *    with the "ROLE_" prefix required by Spring Security's role-based access control
     * 4. Returns a UserDetails object containing the username, hashed password, and authorities
     *
     * The "ROLE_" prefix is a Spring Security convention that allows using
     * hasRole("ADMIN") in security expressions, which internally checks for "ROLE_ADMIN".
     *
     * @param username the username identifying the user whose data is required
     * @return a fully populated UserDetails object for authentication
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Attempt to find the user in the database by their unique username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        // Convert the user's application role into a Spring Security GrantedAuthority.
        // The "ROLE_" prefix is required by Spring Security for role-based method security
        // annotations like @PreAuthorize("hasRole('ADMIN')").
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        // Build and return the Spring Security UserDetails object.
        // This object encapsulates the user's credentials and authorities for the
        // authentication and authorisation pipeline.
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
