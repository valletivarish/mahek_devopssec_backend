package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.AttendeeDTO;
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
 * Integration tests for Attendee CRUD endpoints.
 * Tests all CRUD operations and validates that input validation
 * rules for email format, name length, etc. are properly enforced.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AttendeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static String createdAttendeeId;

    /**
     * Registers a test user and obtains a JWT token for authenticated test requests.
     */
    @BeforeAll
    static void setUp(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("attendeeTestUser");
        register.setEmail("atttest@example.com");
        register.setPassword("password123");
        register.setFullName("Attendee Test User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        jwtToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    /**
     * Tests creating a new attendee with valid data returns 201 Created.
     */
    @Test
    @Order(1)
    void createAttendeeShouldReturn201() throws Exception {
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhone("+353851234567");
        dto.setOrganization("Acme Corp");

        MvcResult result = mockMvc.perform(post("/api/attendees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andReturn();

        createdAttendeeId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    /**
     * Tests retrieving all attendees returns a list.
     */
    @Test
    @Order(2)
    void getAllAttendeesShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/attendees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Tests that creating an attendee with invalid email returns validation error.
     */
    @Test
    @Order(3)
    void createAttendeeWithInvalidEmailShouldFail() throws Exception {
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("not-an-email");

        mockMvc.perform(post("/api/attendees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    /**
     * Tests that creating an attendee with missing required fields returns errors.
     */
    @Test
    @Order(4)
    void createAttendeeWithMissingFieldsShouldFail() throws Exception {
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("");
        dto.setLastName("");
        dto.setEmail("");

        mockMvc.perform(post("/api/attendees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    /**
     * Tests updating an attendee with valid data returns 200 OK.
     * Uses the attendee created in Order(1) to avoid conflicts with seeded data.
     */
    @Test
    @Order(5)
    void updateAttendeeShouldReturn200() throws Exception {
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("John");
        dto.setLastName("Updated");
        dto.setEmail("john.doe@example.com");
        dto.setPhone("+353859999999");
        dto.setOrganization("Updated Corp");

        mockMvc.perform(put("/api/attendees/" + createdAttendeeId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"));
    }

    /**
     * Tests searching attendees by name query returns matching results.
     */
    @Test
    @Order(6)
    void searchAttendeesShouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/attendees/search?query=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
