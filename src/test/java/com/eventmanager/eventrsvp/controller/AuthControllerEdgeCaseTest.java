package com.eventmanager.eventrsvp.controller;

import com.eventmanager.eventrsvp.dto.LoginRequest;
import com.eventmanager.eventrsvp.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void registerWithShortUsernameShouldFail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab");
        request.setEmail("short@example.com");
        request.setPassword("password123");
        request.setFullName("Short Name");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    @Order(2)
    void registerWithShortPasswordShouldFail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("validuser");
        request.setEmail("validuser@example.com");
        request.setPassword("12345");
        request.setFullName("Valid Name");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @Order(3)
    void registerValidUserAndLoginSuccessfullyShouldWork() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("edgeuser");
        register.setEmail("edgeuser@example.com");
        register.setPassword("password123");
        register.setFullName("Edge User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").exists());

        LoginRequest login = new LoginRequest();
        login.setUsername("edgeuser");
        login.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @Order(4)
    void registerDuplicateUsernameShouldFail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("edgeuser");
        request.setEmail("different@example.com");
        request.setPassword("password123");
        request.setFullName("Duplicate User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void registerDuplicateEmailShouldFail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("differentuser");
        request.setEmail("edgeuser@example.com");
        request.setPassword("password123");
        request.setFullName("Duplicate Email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    void loginWithWrongPasswordShouldFail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("edgeuser");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    void loginWithNonExistentUserShouldFail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("ghostuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    void registerWithAllBlankFieldsShouldReturnMultipleErrors() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setEmail("");
        request.setPassword("");
        request.setFullName("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @Order(9)
    void registerWithInvalidEmailFormatShouldFail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("invalidemail");
        request.setEmail("not-an-email");
        request.setPassword("password123");
        request.setFullName("Invalid Email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @Order(10)
    void registerWithMinimumValidInputsShouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abc");
        request.setEmail("min@e.co");
        request.setPassword("123456");
        request.setFullName("M");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
