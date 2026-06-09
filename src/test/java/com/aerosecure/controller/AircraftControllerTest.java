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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    // --- getAllAircraft ---

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void getAllAircraft_AsEngineer_Success() throws Exception {
        AircraftDTO aircraftDTO = AircraftDTO.builder()
                .aircraftId(1L)
                .model("Boeing 737 Max")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();
        org.springframework.data.domain.Page<AircraftDTO> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(aircraftDTO));

        when(aircraftService.getAllAircraft(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/aircraft")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "aircraftId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Aircraft retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].aircraftId").value(1L));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllAircraft_AsUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/aircraft"))
                .andExpect(status().isForbidden());
    }

    // --- getAircraftById ---

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void getAircraftById_Exists_Success() throws Exception {
        AircraftDTO responseDTO = AircraftDTO.builder()
                .aircraftId(1L)
                .model("Boeing 737 Max")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        when(aircraftService.getAircraftById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/aircraft/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Aircraft retrieved successfully"))
                .andExpect(jsonPath("$.data.aircraftId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void getAircraftById_NotExists_NotFound() throws Exception {
        when(aircraftService.getAircraftById(99L)).thenThrow(new com.aerosecure.exception.ResourceNotFoundException("Aircraft not found with ID: 99"));

        mockMvc.perform(get("/api/aircraft/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Aircraft not found with ID: 99"));
    }

    // --- updateAircraft ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateAircraft_AsAdmin_Success() throws Exception {
        AircraftDTO requestDTO = AircraftDTO.builder()
                .model("Boeing 777")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        AircraftDTO responseDTO = AircraftDTO.builder()
                .aircraftId(1L)
                .model("Boeing 777")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        when(aircraftService.updateAircraft(any(Long.class), any(AircraftDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/aircraft/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Aircraft updated successfully"))
                .andExpect(jsonPath("$.data.aircraftId").value(1L))
                .andExpect(jsonPath("$.data.model").value("Boeing 777"));
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void updateAircraft_AsEngineer_Forbidden() throws Exception {
        AircraftDTO requestDTO = AircraftDTO.builder()
                .model("Boeing 777")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/aircraft/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    // --- deleteAircraft ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteAircraft_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/aircraft/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Aircraft deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void deleteAircraft_AsEngineer_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/aircraft/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- getAircraftByStatus ---

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void getAircraftByStatus_ValidStatus_Success() throws Exception {
        AircraftDTO aircraftDTO = AircraftDTO.builder()
                .aircraftId(1L)
                .model("Boeing 737 Max")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        when(aircraftService.getAircraftByStatus(AircraftStatus.ACTIVE)).thenReturn(java.util.List.of(aircraftDTO));

        mockMvc.perform(get("/api/aircraft/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Aircraft filtered by status: ACTIVE"))
                .andExpect(jsonPath("$.data[0].aircraftId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void getAircraftByStatus_InvalidStatus_BadRequest() throws Exception {
        mockMvc.perform(get("/api/aircraft/status/INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    // --- searchByManufacturer ---

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void searchByManufacturer_ValidParam_Success() throws Exception {
        AircraftDTO aircraftDTO = AircraftDTO.builder()
                .aircraftId(1L)
                .model("Boeing 737 Max")
                .manufacturer("Boeing")
                .status(AircraftStatus.ACTIVE)
                .build();

        when(aircraftService.searchByManufacturer("Boeing")).thenReturn(java.util.List.of(aircraftDTO));

        mockMvc.perform(get("/api/aircraft/search")
                        .param("manufacturer", "Boeing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Aircraft search results for: Boeing"))
                .andExpect(jsonPath("$.data[0].aircraftId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void searchByManufacturer_MissingParam_BadRequest() throws Exception {
        mockMvc.perform(get("/api/aircraft/search"))
                .andExpect(status().isBadRequest());
    }
}
