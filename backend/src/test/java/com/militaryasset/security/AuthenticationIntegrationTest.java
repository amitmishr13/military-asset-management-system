package com.militaryasset.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.militaryasset.module.auth.dto.LoginRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Valid Admin Login Returns JWT Token and User Information")
    public void testValidAdminLogin() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.username", is("admin")))
                .andExpect(jsonPath("$.data.role", is("ADMIN")));
    }

    @Test
    @DisplayName("Valid Base Commander Login Returns Base Assignment Data")
    public void testValidCommanderLogin() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("commander_alpha", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.username", is("commander_alpha")))
                .andExpect(jsonPath("$.data.role", is("BASE_COMMANDER")))
                .andExpect(jsonPath("$.data.baseId", notNullValue()))
                .andExpect(jsonPath("$.data.baseCode", is("BASE_ALPHA")));
    }

    @Test
    @DisplayName("Valid Logistics Officer Login Returns Role Information")
    public void testValidLogisticsOfficerLogin() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("logistics_officer", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.role", is("LOGISTICS_OFFICER")));
    }

    @Test
    @DisplayName("Invalid Password Rejects Authentication with HTTP 401")
    public void testInvalidPasswordLogin() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Unknown Username Rejects Authentication with HTTP 401")
    public void testUnknownUserLogin() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("unknown_user", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Missing Credentials Return HTTP 400 Bad Request")
    public void testMissingCredentialsLogin() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Protected Endpoints Require Bearer Token (HTTP 401 Without Token)")
    public void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test/admin-only"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Invalid Bearer Token Rejects Access with HTTP 401")
    public void testInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/test/admin-only")
                        .header("Authorization", "Bearer invalid.jwt.token.string"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
