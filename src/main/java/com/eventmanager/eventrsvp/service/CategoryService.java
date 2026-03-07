package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.CategoryDTO;
import com.eventmanager.eventrsvp.dto.CategoryResponse;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.Category;
import com.eventmanager.eventrsvp.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing event categories.
 *
 * Categories provide a classification system for events (e.g., Conference, Workshop,
 * Seminar, Social). They help users filter and organise events, and each category
 * can have a colour code for visual distinction in the frontend UI.
 *
 * This service enforces the business rule that category names must be unique across
 * the system. When updating a category, the uniqueness check excludes the category
 * being updated to allow renaming while still preventing name collisions with
 * other categories.
 *
 * Read operations use @Transactional(readOnly = true) to optimise database
 * performance by signalling to the JPA provider that no flush is needed.
 */
@Service
public class CategoryService {

    /** Repository for accessing and persisting category data */
    private final CategoryRepository categoryRepository;

    /**
     * Constructor injection of the CategoryRepository dependency.
     *
     * @param categoryRepository the repository for category CRUD operations
     */
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Retrieves all categories from the database and converts them to response DTOs.
     *
     * This method fetches every category record and maps each entity to a
     * CategoryResponse DTO. The response DTOs contain only the fields needed
     * by the frontend, providing a clean separation between the persistence
     * layer and the API contract.
     *
     * @return a list of all categories as CategoryResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        // Fetch all categories and transform each entity to a response DTO
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single category by its unique identifier.
     *
     * Throws a ResourceNotFoundException if no category exists with the given ID,
     * which is translated to a 404 HTTP response by the GlobalExceptionHandler.
     *
     * @param id the unique identifier of the category to retrieve
     * @return the category data as a CategoryResponse DTO
     * @throws ResourceNotFoundException if no category is found with the given ID
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        // Look up the category by ID, throwing a 404 if not found
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return mapToResponse(category);
    }

    /**
     * Creates a new category after validating that the name is unique.
     *
     * Category names must be unique across the entire system to prevent confusion
     * when filtering events. If a category with the same name already exists,
     * a BadRequestException is thrown to inform the client.
     *
     * The createdAt timestamp is automatically set by the @PrePersist callback
     * on the Category entity.
     *
     * @param categoryDTO the DTO containing the new category's name, description, and colour code
     * @return the created category as a CategoryResponse DTO
     * @throws BadRequestException if a category with the same name already exists
     */
    @Transactional
    public CategoryResponse createCategory(CategoryDTO categoryDTO) {
        // Enforce unique category name constraint before persisting
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new BadRequestException(
                    "Category with name '" + categoryDTO.getName() + "' already exists");
        }

        // Build the Category entity from the DTO fields
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .colorCode(categoryDTO.getColorCode())
                .build();

        // Persist the new category to the database
        Category savedCategory = categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }

    /**
     * Updates an existing category with new data.
     *
     * The uniqueness check for the category name excludes the category being updated.
     * This allows a category to keep its current name (no false positive on self-match)
     * while still preventing it from taking a name that belongs to another category.
     *
     * @param id          the unique identifier of the category to update
     * @param categoryDTO the DTO containing the updated category data
     * @return the updated category as a CategoryResponse DTO
     * @throws ResourceNotFoundException if no category is found with the given ID
     * @throws BadRequestException       if the new name conflicts with an existing category
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryDTO categoryDTO) {
        // Retrieve the existing category, throwing 404 if not found
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        // Check if the new name conflicts with another existing category.
        // We allow the category to retain its own name by checking if the found
        // category with that name has a different ID than the one being updated.
        categoryRepository.findByName(categoryDTO.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException(
                                "Category with name '" + categoryDTO.getName() + "' already exists");
                    }
                });

        // Apply the updated field values to the existing entity
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setColorCode(categoryDTO.getColorCode());

        // Persist the changes to the database
        Category updatedCategory = categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }

    /**
     * Deletes a category by its unique identifier.
     *
     * Note: If events reference this category, the database foreign key constraint
     * will prevent deletion and throw a DataIntegrityViolationException, which
     * should be handled by the GlobalExceptionHandler.
     *
     * @param id the unique identifier of the category to delete
     * @throws ResourceNotFoundException if no category is found with the given ID
     */
    @Transactional
    public void deleteCategory(Long id) {
        // Verify the category exists before attempting deletion
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }

        categoryRepository.deleteById(id);
    }

    /**
     * Maps a Category entity to a CategoryResponse DTO using the builder pattern.
     *
     * This private helper method centralises the entity-to-DTO conversion logic
     * to avoid duplication across the service methods.
     *
     * @param category the Category entity to convert
     * @return the corresponding CategoryResponse DTO
     */
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .colorCode(category.getColorCode())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
