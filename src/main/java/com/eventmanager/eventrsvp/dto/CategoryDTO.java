package com.eventmanager.eventrsvp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for creating and updating event categories.
 * Validates that the category name is present and colour code matches hex format.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    /** Category name, required and unique, max 100 characters */
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    /** Optional description of the category */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /** Hex colour code for visual identification, must match #RRGGBB format */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Colour code must be a valid hex colour (e.g., #FF5733)")
    private String colorCode;
}
