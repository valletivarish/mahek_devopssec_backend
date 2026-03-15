package com.eventmanager.eventrsvp.controller;

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
class DashboardControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void dashboardShouldReturnAllExpectedFields() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").isNumber())
                .andExpect(jsonPath("$.upcomingEvents").isNumber())
                .andExpect(jsonPath("$.totalRsvps").isNumber())
                .andExpect(jsonPath("$.confirmedRsvps").isNumber())
                .andExpect(jsonPath("$.totalAttendees").isNumber())
                .andExpect(jsonPath("$.totalCheckIns").isNumber())
                .andExpect(jsonPath("$.attendanceRate").isNumber())
                .andExpect(jsonPath("$.rsvpsByStatus").exists())
                .andExpect(jsonPath("$.eventsByCategory").exists())
                .andExpect(jsonPath("$.recentEvents").isArray());
    }

    @Test
    @Order(2)
    void dashboardTotalsShouldBeNonNegative() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        var tree = objectMapper.readTree(json);

        assertTrue(tree.get("totalEvents").asInt() >= 0);
        assertTrue(tree.get("upcomingEvents").asInt() >= 0);
        assertTrue(tree.get("totalRsvps").asInt() >= 0);
        assertTrue(tree.get("confirmedRsvps").asInt() >= 0);
        assertTrue(tree.get("totalAttendees").asInt() >= 0);
        assertTrue(tree.get("totalCheckIns").asInt() >= 0);
        assertTrue(tree.get("attendanceRate").asDouble() >= 0.0);
    }

    @Test
    @Order(3)
    void dashboardRecentEventsShouldHaveExpectedStructure() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recentEvents[0].id").exists())
                .andExpect(jsonPath("$.recentEvents[0].title").exists())
                .andExpect(jsonPath("$.recentEvents[0].eventDate").exists())
                .andExpect(jsonPath("$.recentEvents[0].location").exists())
                .andExpect(jsonPath("$.recentEvents[0].status").exists());
    }

    @Test
    @Order(4)
    void dashboardRsvpsByStatusShouldContainAllStatuses() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsvpsByStatus.CONFIRMED").exists())
                .andExpect(jsonPath("$.rsvpsByStatus.DECLINED").exists())
                .andExpect(jsonPath("$.rsvpsByStatus.MAYBE").exists())
                .andExpect(jsonPath("$.rsvpsByStatus.WAITLISTED").exists());
    }

    @Test
    @Order(5)
    void dashboardShouldReturnContentTypeJson() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    private void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}
