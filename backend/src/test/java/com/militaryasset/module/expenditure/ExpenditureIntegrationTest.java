package com.militaryasset.module.expenditure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.militaryasset.module.auth.dto.LoginRequestDTO;
import com.militaryasset.module.expenditure.dto.ExpenditureCreateDTO;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ExpenditureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String logisticsToken;
    private String commanderAlphaToken;
    private String commanderBravoToken;
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

        MvcResult bravoResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO("commander_bravo", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();

        String bravoResponse = bravoResult.getResponse().getContentAsString();
        commanderBravoToken = objectMapper.readTree(bravoResponse).path("data").path("token").asText();
        bravoBaseId = objectMapper.readTree(bravoResponse).path("data").path("baseId").asLong();
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
    @DisplayName("Admin Can Record Unlinked Asset Expenditure (HTTP 201)")
    public void testAdminCreateUnlinkedExpenditure() throws Exception {
        // Equipment ID 4 (5.56mm ammo at Alpha Base)
        ExpenditureCreateDTO dto = new ExpenditureCreateDTO(
                alphaBaseId, 4L, null, 100, "Routine live-fire drill", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/expenditures")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.expenditureReference", startsWith("EXP-")))
                .andExpect(jsonPath("$.data.expendedQuantity", is(100)))
                .andExpect(jsonPath("$.data.recordedByUsername", is("admin")));
    }

    @Test
    @DisplayName("Base Commander Can Record Expenditure Only For Assigned Base")
    public void testCommanderExpenditurePermissions() throws Exception {
        // Commander Alpha recording for Alpha Base -> SUCCESS
        ExpenditureCreateDTO validDto = new ExpenditureCreateDTO(
                alphaBaseId, 4L, null, 50, "Perimeter security test", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/expenditures")
                        .header("Authorization", "Bearer " + commanderAlphaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());

        // Commander Alpha recording for Bravo Base -> DENIED (HTTP 403)
        ExpenditureCreateDTO invalidDto = new ExpenditureCreateDTO(
                bravoBaseId, 4L, null, 50, "Cross-base unauthorized expenditure", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/expenditures")
                        .header("Authorization", "Bearer " + commanderAlphaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Logistics Officer Is Denied Access To Expenditures (HTTP 403)")
    public void testLogisticsOfficerDeniedExpenditure() throws Exception {
        ExpenditureCreateDTO dto = new ExpenditureCreateDTO(
                alphaBaseId, 4L, null, 10, "Unauthorized officer expenditure", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/expenditures")
                        .header("Authorization", "Bearer " + logisticsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Expenditure Fails For Invalid Base Or Invalid Quantity")
    public void testInvalidExpenditureRequests() throws Exception {
        // Invalid Base ID
        ExpenditureCreateDTO invalidBaseDto = new ExpenditureCreateDTO(
                9999L, 4L, null, 10, "Invalid base test", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/expenditures")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBaseDto)))
                .andExpect(status().isNotFound());

        // Quantity <= 0
        ExpenditureCreateDTO zeroQuantityDto = new ExpenditureCreateDTO(
                alphaBaseId, 4L, null, 0, "Zero quantity test", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/expenditures")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroQuantityDto)))
                .andExpect(status().isBadRequest());

        // Excessive quantity exceeding available stock
        ExpenditureCreateDTO excessiveDto = new ExpenditureCreateDTO(
                alphaBaseId, 4L, null, 999999, "Excessive quantity test", LocalDateTime.now()
        );

        mockMvc.perform(post("/api/expenditures")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(excessiveDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Retrieve Expenditure History With Filtering And Base Restriction")
    public void testGetExpendituresHistory() throws Exception {
        // Admin can view globally
        mockMvc.perform(get("/api/expenditures")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(not(empty()))));

        // Commander Alpha attempting to request query parameter baseId = Bravo -> HTTP 403
        mockMvc.perform(get("/api/expenditures")
                        .param("baseId", bravoBaseId.toString())
                        .header("Authorization", "Bearer " + commanderAlphaToken))
                .andExpect(status().isForbidden());
    }
}
