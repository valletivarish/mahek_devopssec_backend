package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.CategoryDTO;
import com.eventmanager.eventrsvp.dto.CategoryResponse;
import com.eventmanager.eventrsvp.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing event categories.
 * Categories allow organisers to group and classify events (e.g., Conferences,
 * Workshops, Meetups) for better filtering and organisation on the frontend.
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Constructor injection of CategoryService which encapsulates
     * all business logic for category CRUD operations.
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Retrieves all event categories in the system.
     * Used by the frontend to populate category dropdowns and filter options.
     *
     * @return 200 OK with a list of all category records
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Retrieves a single category by its unique identifier.
     * Used when viewing or editing a specific category's details.
     *
     * @param id the unique identifier of the category to retrieve
     * @return 200 OK with the category data, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Creates a new event category.
     * Validates that the category name is unique before persisting.
     *
     * @param categoryDTO validated payload containing name, description, and colour code
     * @return 201 Created with the newly created category data including its generated ID
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryResponse created = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing category's details.
     * Allows modification of name, description, and colour code.
     *
     * @param id          the unique identifier of the category to update
     * @param categoryDTO validated payload with the updated category fields
     * @return 200 OK with the updated category data, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("id") Long id,
                                                           @Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryResponse updated = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a category by its unique identifier.
     * Should only be allowed if no events are currently associated with this category.
     *
     * @param id the unique identifier of the category to delete
     * @return 204 No Content on successful deletion, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
