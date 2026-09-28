package com.militaryasset.module.purchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.militaryasset.module.auth.dto.LoginRequestDTO;
import com.militaryasset.module.purchase.dto.PurchaseCreateDTO;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PurchaseIntegrationTest {

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

        String responseString = alphaResult.getResponse().getContentAsString();
        commanderAlphaToken = objectMapper.readTree(responseString).path("data").path("token").asText();
        alphaBaseId = objectMapper.readTree(responseString).path("data").path("baseId").asLong();
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
    @DisplayName("Logistics Officer Can Record Purchase Successfully (HTTP 201)")
    public void testLogisticsOfficerCreatePurchase() throws Exception {
        PurchaseCreateDTO dto = new PurchaseCreateDTO(
                alphaBaseId, 1L, 15, BigDecimal.valueOf(50000.00), LocalDateTime.now(), "Defense Ordnance Suppliers Ltd"
        );

        mockMvc.perform(post("/api/purchases")
                        .header("Authorization", "Bearer " + logisticsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.purchaseReference", startsWith("PUR-")))
                .andExpect(jsonPath("$.data.quantity", is(15)))
                .andExpect(jsonPath("$.data.recordedByUsername", is("logistics_officer")));
    }

    @Test
    @DisplayName("Base Commander Can Record Purchase Only For Assigned Base")
    public void testCommanderPurchasePermissions() throws Exception {
        // Allowed: Commander Alpha recording purchase for Alpha Base
        PurchaseCreateDTO validDto = new PurchaseCreateDTO(
                alphaBaseId, 1L, 5, BigDecimal.valueOf(40000.00), LocalDateTime.now(), "Supplier Alpha"
        );

        mockMvc.perform(post("/api/purchases")
                        .header("Authorization", "Bearer " + commanderAlphaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());

        // Denied: Commander Alpha recording purchase for Bravo Base (HTTP 403)
        PurchaseCreateDTO invalidDto = new PurchaseCreateDTO(
                bravoBaseId, 1L, 5, BigDecimal.valueOf(40000.00), LocalDateTime.now(), "Supplier Bravo"
        );

        mockMvc.perform(post("/api/purchases")
                        .header("Authorization", "Bearer " + commanderAlphaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Invalid Purchase Quantity Rejects With HTTP 400 Bad Request")
    public void testInvalidPurchaseQuantity() throws Exception {
        PurchaseCreateDTO dto = new PurchaseCreateDTO(
                alphaBaseId, 1L, 0, BigDecimal.valueOf(100.00), LocalDateTime.now(), "Invalid Supplier"
        );

        mockMvc.perform(post("/api/purchases")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Retrieve Purchase History With Filtering (HTTP 200)")
    public void testGetPurchasesHistory() throws Exception {
        mockMvc.perform(get("/api/purchases")
                        .param("baseId", alphaBaseId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(not(empty()))));
    }
}
