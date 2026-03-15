package com.eventmanager.eventrsvp.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void getDashboardDataShouldReturnStats() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").isNumber())
                .andExpect(jsonPath("$.upcomingEvents").isNumber())
                .andExpect(jsonPath("$.totalRsvps").isNumber())
                .andExpect(jsonPath("$.confirmedRsvps").isNumber())
                .andExpect(jsonPath("$.totalAttendees").isNumber())
                .andExpect(jsonPath("$.totalCheckIns").isNumber())
                .andExpect(jsonPath("$.attendanceRate").isNumber())
                .andExpect(jsonPath("$.rsvpsByStatus").isMap())
                .andExpect(jsonPath("$.eventsByCategory").isMap())
                .andExpect(jsonPath("$.recentEvents").isArray())
                .andExpect(jsonPath("$.monthlyEventCounts").isMap());
    }

    @Test
    @Order(2)
    void dashboardRsvpStatusDistributionShouldHaveAllStatuses() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsvpsByStatus.CONFIRMED").isNumber())
                .andExpect(jsonPath("$.rsvpsByStatus.DECLINED").isNumber())
                .andExpect(jsonPath("$.rsvpsByStatus.MAYBE").isNumber())
                .andExpect(jsonPath("$.rsvpsByStatus.WAITLISTED").isNumber());
    }
}
