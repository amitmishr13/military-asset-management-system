package com.militaryasset.module.dashboard;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String logisticsToken;
    private String commanderAlphaToken;
    private Long alphaBaseId;
    private Long bravoBaseId;

    @BeforeEach
    public void setupTokens() throws Exception {
        adminToken = obtainToken("admin", "Password123!");
        logisticsToken = obtainToken("logistics_officer", "Password123!");

        MvcResult alphaResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO("commander_alpha", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();

        String alphaResponse = alphaResult.getResponse().getContentAsString();
        commanderAlphaToken = objectMapper.readTree(alphaResponse).path("data").path("token").asText();
        alphaBaseId = objectMapper.readTree(alphaResponse).path("data").path("baseId").asLong();
        bravoBaseId = alphaBaseId + 1;
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequestDTO request = new LoginRequestDTO(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    @Test
    @DisplayName("Admin Can Retrieve Global Dashboard Metrics (HTTP 200)")
    public void testAdminGetDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.openingBalance", notNullValue()))
                .andExpect(jsonPath("$.data.closingBalance", notNullValue()))
                .andExpect(jsonPath("$.data.netMovement", notNullValue()))
                .andExpect(jsonPath("$.data.activeAssignedAssets", notNullValue()))
                .andExpect(jsonPath("$.data.expendedAssets", notNullValue()))
                .andExpect(jsonPath("$.data.availableStock", notNullValue()));
    }

    @Test
    @DisplayName("Base Commander Can Access Dashboard Only For Assigned Base")
    public void testCommanderDashboardPermissions() throws Exception {
        // Allowed: Commander Alpha querying Alpha Base
        mockMvc.perform(get("/api/dashboard")
                        .param("baseId", alphaBaseId.toString())
                        .header("Authorization", "Bearer " + commanderAlphaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.baseId", is(alphaBaseId.intValue())));

        // Denied: Commander Alpha querying Bravo Base -> HTTP 403
        mockMvc.perform(get("/api/dashboard")
                        .param("baseId", bravoBaseId.toString())
                        .header("Authorization", "Bearer " + commanderAlphaToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Logistics Officer Is Denied Dashboard Access (HTTP 403)")
    public void testLogisticsOfficerDeniedDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + logisticsToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Net Movement Detail Modal Endpoint Reconciles Purchases and Transfers (HTTP 200)")
    public void testNetMovementDetailsModal() throws Exception {
        mockMvc.perform(get("/api/dashboard/net-movement-details")
                        .param("date", "2026-01-15")
                        .param("baseId", alphaBaseId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalPurchases", is(10)))
                .andExpect(jsonPath("$.data.netMovement", is(10)))
                .andExpect(jsonPath("$.data.items", is(not(empty()))));
    }

    @Test
    @DisplayName("Reference Endpoints Return Base and Equipment Type Lists")
    public void testReferenceDataEndpoints() throws Exception {
        // GET /api/bases
        mockMvc.perform(get("/api/bases")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(not(empty()))));

        // GET /api/equipment-types
        mockMvc.perform(get("/api/equipment-types")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(not(empty()))));
    }
}
