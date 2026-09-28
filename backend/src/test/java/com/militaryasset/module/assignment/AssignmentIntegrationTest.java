package com.militaryasset.module.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.militaryasset.module.assignment.dto.AssignmentCreateDTO;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AssignmentIntegrationTest {

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
    @DisplayName("Admin Can Create Asset Assignment Successfully (HTTP 201)")
    public void testAdminCreateAssignment() throws Exception {
        AssignmentCreateDTO dto = new AssignmentCreateDTO(
                alphaBaseId, 1L, "Capt. John Doe", "Captain", "MIL-1001", 2, LocalDateTime.now()
        );

        mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.assignmentReference", startsWith("ASN-")))
                .andExpect(jsonPath("$.data.personnelName", is("Capt. John Doe")))
                .andExpect(jsonPath("$.data.assignedQuantity", is(2)))
                .andExpect(jsonPath("$.data.assignedByUsername", is("admin")));
    }

    @Test
    @DisplayName("Base Commander Can Create Assignment For Assigned Base Only")
    public void testCommanderAssignmentPermissions() throws Exception {
        // Commander Alpha creating for Alpha Base -> SUCCESS
        AssignmentCreateDTO validDto = new AssignmentCreateDTO(
                alphaBaseId, 1L, "Sgt. Jane Smith", "Sergeant", "MIL-2002", 1, LocalDateTime.now()
        );

        mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + commanderAlphaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());

        // Commander Alpha creating for Bravo Base -> DENIED (HTTP 403)
        AssignmentCreateDTO invalidDto = new AssignmentCreateDTO(
                bravoBaseId, 1L, "Sgt. Jane Smith", "Sergeant", "MIL-2002", 1, LocalDateTime.now()
        );

        mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + commanderAlphaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Logistics Officer Is Denied Access To Assignments (HTTP 403)")
    public void testLogisticsOfficerDeniedAssignment() throws Exception {
        AssignmentCreateDTO dto = new AssignmentCreateDTO(
                alphaBaseId, 1L, "Pvt. Alex Brown", "Private", "MIL-3003", 1, LocalDateTime.now()
        );

        mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + logisticsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Assignment Creation Fails For Invalid Base Or Equipment (HTTP 404)")
    public void testInvalidBaseOrEquipmentAssignment() throws Exception {
        // Invalid Base ID 9999
        AssignmentCreateDTO invalidBaseDto = new AssignmentCreateDTO(
                9999L, 1L, "Pvt. Alex Brown", "Private", "MIL-3003", 1, LocalDateTime.now()
        );

        mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBaseDto)))
                .andExpect(status().isNotFound());

        // Invalid Equipment ID 9999
        AssignmentCreateDTO invalidEquipDto = new AssignmentCreateDTO(
                alphaBaseId, 9999L, "Pvt. Alex Brown", "Private", "MIL-3003", 1, LocalDateTime.now()
        );

        mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEquipDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Assignment Fails When Requested Quantity Exceeds Available Stock (HTTP 400)")
    public void testExcessiveQuantityAssignment() throws Exception {
        // Requesting 99999 units when available stock is much lower
        AssignmentCreateDTO dto = new AssignmentCreateDTO(
                alphaBaseId, 1L, "Maj. Heavy Demand", "Major", "MIL-9999", 99999, LocalDateTime.now()
        );

        mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Retrieve Assignments History With Filtering And Base Restriction")
    public void testGetAssignmentsHistory() throws Exception {
        // Admin can view globally
        mockMvc.perform(get("/api/assignments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(not(empty()))));

        // Commander Alpha trying to request query parameter baseId = Bravo -> HTTP 403
        mockMvc.perform(get("/api/assignments")
                        .param("baseId", bravoBaseId.toString())
                        .header("Authorization", "Bearer " + commanderAlphaToken))
                .andExpect(status().isForbidden());
    }
}
