package com.eventmanager.eventrsvp.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO for returning category data to the frontend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String colorCode;
    private LocalDateTime createdAt;
}
