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
public class BaseAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String commanderAlphaToken;
    private Long alphaBaseId;
    private Long bravoBaseId;

    @BeforeEach
    public void setupTokens() throws Exception {
        adminToken = obtainToken("admin", "Password123!");
        
        MvcResult alphaResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO("commander_alpha", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = alphaResult.getResponse().getContentAsString();
        commanderAlphaToken = objectMapper.readTree(responseString).path("data").path("token").asText();
        alphaBaseId = objectMapper.readTree(responseString).path("data").path("baseId").asLong();
        bravoBaseId = alphaBaseId + 1; // Assuming Base Bravo has next sequential ID
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
    @DisplayName("Base Commander Allowed Access to Assigned Base (HTTP 200)")
    public void testCommanderAllowedAssignedBase() throws Exception {
        mockMvc.perform(get("/api/test/base/" + alphaBaseId)
                        .header("Authorization", "Bearer " + commanderAlphaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Base Commander Denied Access to Another Base (HTTP 403 Forbidden)")
    public void testCommanderDeniedUnassignedBase() throws Exception {
        mockMvc.perform(get("/api/test/base/" + bravoBaseId)
                        .header("Authorization", "Bearer " + commanderAlphaToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Admin User Allowed Unrestricted Access Across All Bases")
    public void testAdminAllowedAllBases() throws Exception {
        mockMvc.perform(get("/api/test/base/" + alphaBaseId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test/base/" + bravoBaseId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Base Commander Can Initiate Transfer Only When Assigned Base is Source Base")
    public void testCommanderTransferSourceRestriction() throws Exception {
        // Valid: Source is Commander's assigned base (Alpha -> Bravo)
        mockMvc.perform(post("/api/test/transfer-auth")
                        .param("sourceBaseId", alphaBaseId.toString())
                        .param("destinationBaseId", bravoBaseId.toString())
                        .header("Authorization", "Bearer " + commanderAlphaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Invalid: Source is NOT Commander's assigned base (Bravo -> Alpha) -> HTTP 403 Forbidden
        mockMvc.perform(post("/api/test/transfer-auth")
                        .param("sourceBaseId", bravoBaseId.toString())
                        .param("destinationBaseId", alphaBaseId.toString())
                        .header("Authorization", "Bearer " + commanderAlphaToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
