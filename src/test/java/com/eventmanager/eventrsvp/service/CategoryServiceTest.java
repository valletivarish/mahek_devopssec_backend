package com.eventmanager.eventrsvp.service;

import com.eventmanager.eventrsvp.dto.CategoryDTO;
import com.eventmanager.eventrsvp.dto.CategoryResponse;
import com.eventmanager.eventrsvp.exception.BadRequestException;
import com.eventmanager.eventrsvp.exception.ResourceNotFoundException;
import com.eventmanager.eventrsvp.model.Category;
import com.eventmanager.eventrsvp.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L).name("Conference").description("Tech conferences")
                .colorCode("#FF0000").createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllCategoriesShouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        List<CategoryResponse> result = categoryService.getAllCategories();
        assertEquals(1, result.size());
        assertEquals("Conference", result.get(0).getName());
    }

    @Test
    void getCategoryByIdShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        CategoryResponse result = categoryService.getCategoryById(1L);
        assertEquals("Conference", result.getName());
    }

    @Test
    void getCategoryByIdShouldThrowWhenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void createCategoryShouldReturnCreated() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Workshop");
        dto.setDescription("Hands-on workshops");
        dto.setColorCode("#00FF00");

        when(categoryRepository.existsByName("Workshop")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse result = categoryService.createCategory(dto);
        assertNotNull(result);
    }

    @Test
    void createCategoryShouldThrowOnDuplicateName() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Conference");

        when(categoryRepository.existsByName("Conference")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> categoryService.createCategory(dto));
    }

    @Test
    void updateCategoryShouldReturnUpdated() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Conference");
        dto.setDescription("Updated description");
        dto.setColorCode("#0000FF");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Conference")).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse result = categoryService.updateCategory(1L, dto);
        assertNotNull(result);
    }

    @Test
    void updateCategoryShouldThrowOnConflictingName() {
        Category otherCategory = Category.builder().id(2L).name("Workshop").build();

        CategoryDTO dto = new CategoryDTO();
        dto.setName("Workshop");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Workshop")).thenReturn(Optional.of(otherCategory));

        assertThrows(BadRequestException.class, () -> categoryService.updateCategory(1L, dto));
    }

    @Test
    void updateCategoryShouldThrowWhenNotFound() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("X");
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(99L, dto));
    }

    @Test
    void deleteCategoryShouldSucceed() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategoryShouldThrowWhenNotFound() {
        when(categoryRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));
    }
}
