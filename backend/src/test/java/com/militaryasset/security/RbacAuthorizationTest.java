package com.militaryasset.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.militaryasset.module.auth.dto.LoginRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RbacAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String commanderToken;
    private String logisticsToken;

    @BeforeEach
    public void setupTokens() throws Exception {
        adminToken = obtainToken("admin", "Password123!");
        commanderToken = obtainToken("commander_alpha", "Password123!");
        logisticsToken = obtainToken("logistics_officer", "Password123!");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequestDTO request = new LoginRequestDTO(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseString).path("data").path("token").asText();
    }

    @Test
    @DisplayName("Admin Role Can Access Admin Only Endpoints")
    public void testAdminAccessAdminOnly() throws Exception {
        mockMvc.perform(get("/api/test/admin-only")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Base Commander Role Denied Access to Admin Only Endpoints (HTTP 403)")
    public void testCommanderDeniedAdminOnly() throws Exception {
        mockMvc.perform(get("/api/test/admin-only")
                        .header("Authorization", "Bearer " + commanderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Logistics Officer Role Denied Access to Admin Only Endpoints (HTTP 403)")
    public void testLogisticsOfficerDeniedAdminOnly() throws Exception {
        mockMvc.perform(get("/api/test/admin-only")
                        .header("Authorization", "Bearer " + logisticsToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Logistics Officer Allowed Logistics Scope Access")
    public void testLogisticsOfficerAllowedLogisticsScope() throws Exception {
        mockMvc.perform(get("/api/test/logistics-scope")
                        .header("Authorization", "Bearer " + logisticsToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Logistics Officer Denied Assignment Scope Access (HTTP 403)")
    public void testLogisticsOfficerDeniedAssignmentScope() throws Exception {
        mockMvc.perform(get("/api/test/assignment-scope")
                        .header("Authorization", "Bearer " + logisticsToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
