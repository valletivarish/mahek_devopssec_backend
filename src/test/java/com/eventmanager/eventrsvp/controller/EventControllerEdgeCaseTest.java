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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static Long categoryId;
    private static Long createdEventId;

    @BeforeAll
    static void setUp(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("eventEdgeUser");
        register.setEmail("eventedge@example.com");
        register.setPassword("password123");
        register.setFullName("Event Edge User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        jwtToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("EdgeTestCategory");
        categoryDTO.setDescription("Edge case test category");
        categoryDTO.setColorCode("#ABCDEF");

        MvcResult catResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        categoryId = objectMapper.readTree(catResult.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    @Order(1)
    void getEventByNonExistentIdShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/events/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(2)
    void createEventWithNullDateShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("No Date Event")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Somewhere")
                .capacity(50)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.eventDate").exists());
    }

    @Test
    @Order(3)
    void createEventWithNullLocationShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("No Location Event")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .capacity(50)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.location").exists());
    }

    @Test
    @Order(4)
    void createEventWithZeroCapacityShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Zero Capacity Event")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Test")
                .capacity(0)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capacity").exists());
    }

    @Test
    @Order(5)
    void createEventWithNegativeCapacityShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Negative Capacity Event")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Test")
                .capacity(-5)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capacity").exists());
    }

    @Test
    @Order(6)
    void createEventWithNullCapacityShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Null Capacity Event")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Test")
                .categoryId(categoryId)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capacity").exists());
    }

    @Test
    @Order(7)
    void createEventWithNullCategoryIdShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("No Category Event")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Test")
                .capacity(100)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.categoryId").exists());
    }

    @Test
    @Order(8)
    void createEventWithInvalidCategoryIdShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Bad Category Event")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Test")
                .capacity(100)
                .categoryId(99999L)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    void createEventWithMinCapacityShouldSucceed() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Min Capacity Event")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Test")
                .capacity(1)
                .status(EventStatus.UPCOMING)
                .categoryId(categoryId)
                .build();

        MvcResult result = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.capacity").value(1))
                .andReturn();

        createdEventId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    @Order(10)
    void createEventWithMaxCapacityShouldSucceed() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Max Capacity Event")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Test")
                .capacity(10000)
                .status(EventStatus.UPCOMING)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.capacity").value(10000));
    }

    @Test
    @Order(11)
    void updateEventShouldReturn200() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Updated Event Title")
                .eventDate(LocalDate.now().plusDays(20))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .location("Updated Location")
                .capacity(200)
                .status(EventStatus.UPCOMING)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(put("/api/events/" + createdEventId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event Title"));
    }

    @Test
    @Order(12)
    void updateNonExistentEventShouldReturn404() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("Ghost Event")
                .eventDate(LocalDate.now().plusDays(20))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .location("Nowhere")
                .capacity(100)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(put("/api/events/99999")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(13)
    void deleteEventShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/events/" + createdEventId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(14)
    void deleteNonExistentEventShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/events/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(15)
    void getEventsByCategoryShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/events/category/" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(16)
    void searchEventsWithEmptyQueryShouldReturnAll() throws Exception {
        mockMvc.perform(get("/api/events/search?title="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(17)
    void searchEventsWithNoMatchShouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/api/events/search?title=ZZZZNONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Order(18)
    void createEventWithNullStartTimeShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("No Start Time Event")
                .eventDate(LocalDate.now().plusDays(10))
                .endTime(LocalTime.of(17, 0))
                .location("Test")
                .capacity(50)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.startTime").exists());
    }

    @Test
    @Order(19)
    void createEventWithNullEndTimeShouldFail() throws Exception {
        EventDTO dto = EventDTO.builder()
                .title("No End Time Event")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0))
                .location("Test")
                .capacity(50)
                .categoryId(categoryId)
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.endTime").exists());
    }

    @Test
    @Order(20)
    void createEventWithMultipleValidationErrorsShouldReturnAll() throws Exception {
        EventDTO dto = EventDTO.builder().build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.eventDate").exists())
                .andExpect(jsonPath("$.errors.startTime").exists())
                .andExpect(jsonPath("$.errors.endTime").exists())
                .andExpect(jsonPath("$.errors.location").exists())
                .andExpect(jsonPath("$.errors.capacity").exists())
                .andExpect(jsonPath("$.errors.categoryId").exists());
    }
}
