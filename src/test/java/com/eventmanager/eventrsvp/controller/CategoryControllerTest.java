package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.CategoryDTO;
import com.eventmanager.eventrsvp.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Category CRUD endpoints.
 * Tests create, read, update, and delete operations with both valid
 * and invalid input to verify validation rules are enforced.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;

    /**
     * Helper method to register a user and obtain a JWT token for authenticated requests.
     */
    @BeforeAll
    static void setUp(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("categoryTestUser");
        register.setEmail("cattest@example.com");
        register.setPassword("password123");
        register.setFullName("Category Test User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(response).get("token").asText();
    }

    /**
     * Tests creating a new category with valid data returns 201 Created.
     */
    @Test
    @Order(1)
    void createCategoryShouldReturn201() throws Exception {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Hackathon");
        dto.setDescription("Competitive coding and innovation events");
        dto.setColorCode("#3B82F6");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hackathon"))
                .andExpect(jsonPath("$.colorCode").value("#3B82F6"));
    }

    /**
     * Tests that retrieving all categories returns a non-empty list.
     */
    @Test
    @Order(2)
    void getAllCategoriesShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Conference"));
    }

    /**
     * Tests that retrieving a category by ID returns the correct data.
     */
    @Test
    @Order(3)
    void getCategoryByIdShouldReturnCategory() throws Exception {
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conference"));
    }

    /**
     * Tests updating a category with valid data returns the updated record.
     */
    @Test
    @Order(4)
    void updateCategoryShouldReturn200() throws Exception {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Tech Conference");
        dto.setDescription("Technology conferences and summits");
        dto.setColorCode("#2563EB");

        mockMvc.perform(put("/api/categories/1")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tech Conference"));
    }

    /**
     * Tests that creating a category with blank name returns validation error.
     */
    @Test
    @Order(5)
    void createCategoryWithBlankNameShouldReturnBadRequest() throws Exception {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("");
        dto.setDescription("Missing name");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    /**
     * Tests that creating a category with invalid colour code returns validation error.
     */
    @Test
    @Order(6)
    void createCategoryWithInvalidColorShouldReturnBadRequest() throws Exception {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Workshop");
        dto.setColorCode("invalid");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.colorCode").exists());
    }

    /**
     * Tests deleting a category returns 204 No Content.
     */
    @Test
    @Order(7)
    void deleteCategoryShouldReturn204() throws Exception {
        // First create a category to delete
        CategoryDTO dto = new CategoryDTO();
        dto.setName("ToDelete");
        dto.setDescription("Will be deleted");
        dto.setColorCode("#FF0000");

        MvcResult createResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract the ID of the newly created category and delete it
        String createdId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(delete("/api/categories/" + createdId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}
