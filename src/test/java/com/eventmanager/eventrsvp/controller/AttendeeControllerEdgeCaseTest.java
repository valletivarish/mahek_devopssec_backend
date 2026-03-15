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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AttendeeControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static String attendeeId;

    @BeforeAll
    static void setUp(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("attendeeEdgeUser");
        register.setEmail("attedge@example.com");
        register.setPassword("password123");
        register.setFullName("Attendee Edge User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        jwtToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    @Order(1)
    void getAttendeeByNonExistentIdShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/attendees/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(2)
    void createAttendeeWithOnlyRequiredFieldsShouldSucceed() throws Exception {
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("Minimal");
        dto.setLastName("Attendee");
        dto.setEmail("minimal.edge@example.com");

        MvcResult result = mockMvc.perform(post("/api/attendees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Minimal"))
                .andReturn();

        attendeeId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    @Order(3)
    void createAttendeeWithDuplicateEmailShouldFail() throws Exception {
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("Duplicate");
        dto.setLastName("Email");
        dto.setEmail("minimal.edge@example.com");

        mockMvc.perform(post("/api/attendees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void updateAttendeeToConflictingEmailShouldFail() throws Exception {
        // Create a second attendee
        AttendeeDTO dto2 = new AttendeeDTO();
        dto2.setFirstName("Second");
        dto2.setLastName("Person");
        dto2.setEmail("second.edge@example.com");

        MvcResult result2 = mockMvc.perform(post("/api/attendees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated())
                .andReturn();

        String secondId = objectMapper.readTree(result2.getResponse().getContentAsString())
                .get("id").asText();

        // Try to update second attendee to have first attendee's email
        AttendeeDTO updateDto = new AttendeeDTO();
        updateDto.setFirstName("Second");
        updateDto.setLastName("Person");
        updateDto.setEmail("minimal.edge@example.com");

        mockMvc.perform(put("/api/attendees/" + secondId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void deleteAttendeeNonExistentShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/attendees/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void searchAttendeesWithNoMatchShouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/api/attendees/search?query=ZZZZNONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Order(7)
    void createAttendeeWithoutAuthShouldFail() throws Exception {
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("NoAuth");
        dto.setLastName("User");
        dto.setEmail("noauth@example.com");

        mockMvc.perform(post("/api/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    void updateAttendeeWithSameEmailShouldSucceed() throws Exception {
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("Updated");
        dto.setLastName("Attendee");
        dto.setEmail("minimal.edge@example.com");
        dto.setPhone("+1234567890");

        mockMvc.perform(put("/api/attendees/" + attendeeId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    @Order(9)
    void updateNonExistentAttendeeShouldReturn404() throws Exception {
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("Ghost");
        dto.setLastName("Attendee");
        dto.setEmail("ghost@example.com");

        mockMvc.perform(put("/api/attendees/99999")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    void deleteAttendeeShouldReturn204() throws Exception {
        // Create expendable attendee
        AttendeeDTO dto = new AttendeeDTO();
        dto.setFirstName("ToDelete");
        dto.setLastName("Edge");
        dto.setEmail("todelete.edge@example.com");

        MvcResult result = mockMvc.perform(post("/api/attendees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String deleteId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(delete("/api/attendees/" + deleteId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}
