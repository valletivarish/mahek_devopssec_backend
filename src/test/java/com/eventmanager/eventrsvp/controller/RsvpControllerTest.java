package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.*;

import com.eventmanager.eventrsvp.model.EventStatus;
import com.eventmanager.eventrsvp.model.RsvpStatus;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RsvpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static Long eventId;
    private static Long attendeeId;
    private static Long rsvpId;

    @BeforeAll
    static void setUp(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // Register test user
        RegisterRequest register = new RegisterRequest();
        register.setUsername("rsvpTestUser");
        register.setEmail("rsvptest@example.com");
        register.setPassword("password123");
        register.setFullName("RSVP Test User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        jwtToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();

        // Create a category
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("RsvpTestCategory");
        categoryDTO.setDescription("Category for RSVP tests");
        categoryDTO.setColorCode("#FF5733");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isCreated());

        // Create an event
        EventDTO eventDTO = EventDTO.builder()
                .title("RSVP Test Event")
                .description("Event for testing RSVPs")
                .eventDate(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .location("Test Venue")
                .capacity(100)
                .status(EventStatus.UPCOMING)
                .categoryId(1L)
                .build();

        MvcResult eventResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        eventId = objectMapper.readTree(eventResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Create an attendee
        AttendeeDTO attendeeDTO = AttendeeDTO.builder()
                .firstName("RSVP")
                .lastName("Tester")
                .email("rsvptester@example.com")
                .phone("1234567890")
                .organization("Test Org")
                .build();

        MvcResult attendeeResult = mockMvc.perform(post("/api/attendees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attendeeDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        attendeeId = objectMapper.readTree(attendeeResult.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    @Order(1)
    void createRsvpShouldReturn201() throws Exception {
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(eventId)
                .attendeeId(attendeeId)
                .status(RsvpStatus.CONFIRMED)
                .dietaryPreferences("Vegetarian")
                .specialRequirements("Wheelchair access")
                .build();

        MvcResult result = mockMvc.perform(post("/api/rsvps")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.dietaryPreferences").value("Vegetarian"))
                .andReturn();

        rsvpId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    @Order(2)
    void getAllRsvpsShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/rsvps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void getRsvpByIdShouldReturnRsvp() throws Exception {
        mockMvc.perform(get("/api/rsvps/" + rsvpId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rsvpId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @Order(4)
    void getRsvpsByEventShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/rsvps/event/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].eventId").value(eventId));
    }

    @Test
    @Order(5)
    void getRsvpsByAttendeeShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/rsvps/attendee/" + attendeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].attendeeId").value(attendeeId));
    }

    @Test
    @Order(6)
    void updateRsvpShouldReturn200() throws Exception {
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(eventId)
                .attendeeId(attendeeId)
                .status(RsvpStatus.MAYBE)
                .dietaryPreferences("Vegan")
                .specialRequirements("None")
                .build();

        mockMvc.perform(put("/api/rsvps/" + rsvpId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAYBE"))
                .andExpect(jsonPath("$.dietaryPreferences").value("Vegan"));
    }

    @Test
    @Order(7)
    void createDuplicateRsvpShouldFail() throws Exception {
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(eventId)
                .attendeeId(attendeeId)
                .status(RsvpStatus.CONFIRMED)
                .build();

        mockMvc.perform(post("/api/rsvps")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    void createRsvpWithoutEventIdShouldFail() throws Exception {
        RsvpDTO dto = RsvpDTO.builder()
                .attendeeId(attendeeId)
                .status(RsvpStatus.CONFIRMED)
                .build();

        mockMvc.perform(post("/api/rsvps")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.eventId").exists());
    }

    @Test
    @Order(9)
    void createRsvpWithoutStatusShouldFail() throws Exception {
        RsvpDTO dto = RsvpDTO.builder()
                .eventId(eventId)
                .attendeeId(attendeeId)
                .build();

        mockMvc.perform(post("/api/rsvps")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());
    }

    @Test
    @Order(10)
    void deleteRsvpShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/rsvps/" + rsvpId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(11)
    void getRsvpByInvalidIdShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/rsvps/99999"))
                .andExpect(status().isNotFound());
    }
}
