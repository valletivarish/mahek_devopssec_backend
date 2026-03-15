package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.*;
import com.eventmanager.eventrsvp.model.CheckInMethod;
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
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static Long eventId;
    private static Long attendeeId;
    private static Long checkInId;

    @BeforeAll
    static void setUp(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // Register test user
        RegisterRequest register = new RegisterRequest();
        register.setUsername("checkinTestUser");
        register.setEmail("checkintest@example.com");
        register.setPassword("password123");
        register.setFullName("CheckIn Test User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        jwtToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();

        // Create a category
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("CheckInTestCategory");
        categoryDTO.setDescription("Category for check-in tests");
        categoryDTO.setColorCode("#33FF57");

        MvcResult catResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId = objectMapper.readTree(catResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Create an event
        EventDTO eventDTO = EventDTO.builder()
                .title("CheckIn Test Event")
                .description("Event for testing check-ins")
                .eventDate(LocalDate.now().plusDays(15))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .location("Test Hall")
                .capacity(200)
                .status(EventStatus.UPCOMING)
                .categoryId(categoryId)
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
                .firstName("CheckIn")
                .lastName("Tester")
                .email("checkintester@example.com")
                .phone("9876543210")
                .organization("Test Corp")
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
    void createCheckInShouldReturn201() throws Exception {
        CheckInDTO dto = CheckInDTO.builder()
                .eventId(eventId)
                .attendeeId(attendeeId)
                .checkInMethod(CheckInMethod.QR_CODE)
                .notes("Arrived on time")
                .build();

        MvcResult result = mockMvc.perform(post("/api/checkins")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkInMethod").value("QR_CODE"))
                .andExpect(jsonPath("$.notes").value("Arrived on time"))
                .andReturn();

        checkInId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    @Order(2)
    void getAllCheckInsShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/checkins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(3)
    void getCheckInByIdShouldReturnCheckIn() throws Exception {
        mockMvc.perform(get("/api/checkins/" + checkInId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(checkInId))
                .andExpect(jsonPath("$.checkInMethod").value("QR_CODE"));
    }

    @Test
    @Order(4)
    void getCheckInsByEventShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/checkins/event/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].eventId").value(eventId));
    }

    @Test
    @Order(5)
    void createDuplicateCheckInShouldFail() throws Exception {
        CheckInDTO dto = CheckInDTO.builder()
                .eventId(eventId)
                .attendeeId(attendeeId)
                .checkInMethod(CheckInMethod.MANUAL)
                .build();

        mockMvc.perform(post("/api/checkins")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    void createCheckInWithoutEventIdShouldFail() throws Exception {
        CheckInDTO dto = CheckInDTO.builder()
                .attendeeId(attendeeId)
                .checkInMethod(CheckInMethod.QR_CODE)
                .build();

        mockMvc.perform(post("/api/checkins")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.eventId").exists());
    }

    @Test
    @Order(7)
    void createCheckInWithoutMethodShouldFail() throws Exception {
        CheckInDTO dto = CheckInDTO.builder()
                .eventId(eventId)
                .attendeeId(attendeeId)
                .build();

        mockMvc.perform(post("/api/checkins")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.checkInMethod").exists());
    }

    @Test
    @Order(8)
    void deleteCheckInShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/checkins/" + checkInId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(9)
    void getCheckInByInvalidIdShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/checkins/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    void deleteNonExistentCheckInShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/checkins/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
}
