package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.CategoryDTO;
import com.eventmanager.eventrsvp.dto.EventDTO;
import com.eventmanager.eventrsvp.dto.RegisterRequest;
import com.eventmanager.eventrsvp.model.EventStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Event CRUD endpoints.
 * Tests event creation with category validation, status filtering,
 * capacity validation, and input validation rules for dates and times.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;

    /**
     * Sets up a test user and creates a category needed for event creation.
     */
    @BeforeAll
    static void setUp(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // Register test user
        RegisterRequest register = new RegisterRequest();
        register.setUsername("eventTestUser");
        register.setEmail("eventtest@example.com");
        register.setPassword("password123");
        register.setFullName("Event Test User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        jwtToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();

        // Create a category for events to reference
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("EventTestCategory");
        categoryDTO.setDescription("Category for event tests");
        categoryDTO.setColorCode("#10B981");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isCreated());
    }

    /**
     * Tests creating a new event with valid data returns 201 Created.
     */
    @Test
    @Order(1)
    void createEventShouldReturn201() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Tech Conference 2025")
                .description("Annual technology conference")
                .eventDate(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Dublin Convention Centre")
                .capacity(500)
                .status(EventStatus.UPCOMING)
                .categoryId(1L)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Tech Conference 2025"))
                .andExpect(jsonPath("$.capacity").value(500));
    }

    /**
     * Tests retrieving all events returns a list.
     */
    @Test
    @Order(2)
    void getAllEventsShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Tech Conference 2025"));
    }

    /**
     * Tests filtering events by status returns matching events.
     */
    @Test
    @Order(3)
    void getEventsByStatusShouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/events/status/UPCOMING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Tests that creating an event with blank title returns validation error.
     */
    @Test
    @Order(4)
    void createEventWithBlankTitleShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("")
                .eventDate(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Dublin")
                .capacity(100)
                .categoryId(1L)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    /**
     * Tests that creating an event with capacity exceeding 10000 returns validation error.
     */
    @Test
    @Order(5)
    void createEventWithExcessiveCapacityShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Huge Event")
                .eventDate(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Stadium")
                .capacity(50000)
                .categoryId(1L)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capacity").exists());
    }

    /**
     * Tests searching events by title keyword returns matching results.
     */
    @Test
    @Order(6)
    void searchEventsByTitleShouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/events/search?title=Tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Tests that creating an event without authentication returns 403.
     */
    @Test
    @Order(7)
    void createEventWithoutAuthShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Unauthorized Event")
                .eventDate(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Nowhere")
                .capacity(100)
                .categoryId(1L)
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
