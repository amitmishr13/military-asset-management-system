package com.militaryasset.module.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.militaryasset.module.auth.dto.LoginRequestDTO;
import com.militaryasset.module.transfer.dto.TransferCreateDTO;
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
public class TransferIntegrationTest {

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
    @DisplayName("Valid Transfer Between Distinct Bases Succeeds (HTTP 201)")
    public void testValidTransferSuccess() throws Exception {
        TransferCreateDTO dto = new TransferCreateDTO(
                alphaBaseId, bravoBaseId, 1L, 2, LocalDateTime.now(), "Routine tactical relocation"
        );

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + logisticsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.transferReference", startsWith("TRN-")))
                .andExpect(jsonPath("$.data.sourceBaseId", is(alphaBaseId.intValue())))
                .andExpect(jsonPath("$.data.destinationBaseId", is(bravoBaseId.intValue())))
                .andExpect(jsonPath("$.data.quantity", is(2)));
    }

    @Test
    @DisplayName("Transfer Between Same Source and Destination Rejects (HTTP 400 Bad Request)")
    public void testSameBaseTransferRejection() throws Exception {
        TransferCreateDTO dto = new TransferCreateDTO(
                alphaBaseId, alphaBaseId, 1L, 2, LocalDateTime.now(), "Self transfer invalid"
        );

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Transfer Exceeding Available Stock Rejects (HTTP 400 Insufficient Stock)")
    public void testExceedingStockTransferRejection() throws Exception {
        TransferCreateDTO dto = new TransferCreateDTO(
                alphaBaseId, bravoBaseId, 1L, 99999, LocalDateTime.now(), "Excessive quantity"
        );

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Insufficient available stock")));
    }

    @Test
    @DisplayName("Base Commander Can Initiate Transfer Only When Assigned Base is Source Base")
    public void testCommanderSourceBaseRestriction() throws Exception {
        // Valid: Commander Alpha transferring FROM Alpha Base (Alpha -> Bravo)
        TransferCreateDTO validDto = new TransferCreateDTO(
                alphaBaseId, bravoBaseId, 1L, 1, LocalDateTime.now(), "Commander initiated transfer"
        );

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + commanderAlphaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());

        // Invalid: Commander Alpha trying to transfer FROM Bravo Base (Bravo -> Alpha) (HTTP 403)
        TransferCreateDTO invalidDto = new TransferCreateDTO(
                bravoBaseId, alphaBaseId, 1L, 1, LocalDateTime.now(), "Unauthorized source base"
        );

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + commanderAlphaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Retrieve Transfer History With Filtering (HTTP 200)")
    public void testGetTransferHistory() throws Exception {
        mockMvc.perform(get("/api/transfers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(not(empty()))));
    }
}
