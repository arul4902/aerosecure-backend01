package com.aerosecure.controller;

import com.aerosecure.dto.AircraftDTO;
import com.aerosecure.entity.AircraftStatus;
import com.aerosecure.security.CustomUserDetailsService;
import com.aerosecure.security.JwtAuthenticationEntryPoint;
import com.aerosecure.security.JwtAuthenticationFilter;
import com.aerosecure.security.JwtTokenProvider;
import com.aerosecure.security.SecurityConfig;
import com.aerosecure.service.AircraftService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AircraftController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
public class AircraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AircraftService aircraftService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Positive Test Case ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createAircraft_AsAdmin_Success() throws Exception {
        AircraftDTO requestDTO = AircraftDTO.builder()
                .model("Boeing 737 Max")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        AircraftDTO responseDTO = AircraftDTO.builder()
                .aircraftId(10L)
                .model("Boeing 737 Max")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        when(aircraftService.createAircraft(any(AircraftDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/aircraft")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Aircraft created successfully"))
                .andExpect(jsonPath("$.data.aircraftId").value(10L))
                .andExpect(jsonPath("$.data.model").value("Boeing 737 Max"))
                .andExpect(jsonPath("$.data.manufacturer").value("Boeing"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    // --- Negative Test Cases ---

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void createAircraft_AsEngineer_Forbidden() throws Exception {
        AircraftDTO requestDTO = AircraftDTO.builder()
                .model("Boeing 737 Max")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/aircraft")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void createAircraft_Unauthenticated_Unauthorized() throws Exception {
        AircraftDTO requestDTO = AircraftDTO.builder()
                .model("Boeing 737 Max")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/aircraft")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createAircraft_InvalidPayload_BadRequest() throws Exception {
        // Missing model, manufacturer and status (nulls/blanks)
        AircraftDTO requestDTO = AircraftDTO.builder()
                .model("")
                .manufacturer("")
                .status(null)
                .build();

        mockMvc.perform(post("/api/aircraft")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.model").value("Model is required"))
                .andExpect(jsonPath("$.data.manufacturer").value("Manufacturer is required"))
                .andExpect(jsonPath("$.data.status").value("Status is required"));
    }
}
