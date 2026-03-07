package com.eventmanager.eventrsvp.repository;

import com.eventmanager.eventrsvp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for Category entity providing CRUD operations
 * and custom queries for unique name validation.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Find a category by its unique name */
    Optional<Category> findByName(String name);

    /** Check if a category with the given name already exists */
    boolean existsByName(String name);
}
