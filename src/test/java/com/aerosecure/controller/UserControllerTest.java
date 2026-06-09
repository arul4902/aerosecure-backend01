package com.aerosecure.controller;

import com.aerosecure.dto.CreateUserRequest;
import com.aerosecure.dto.UserResponseDTO;
import com.aerosecure.security.CustomUserDetailsService;
import com.aerosecure.security.JwtAuthenticationEntryPoint;
import com.aerosecure.security.JwtAuthenticationFilter;
import com.aerosecure.security.JwtTokenProvider;
import com.aerosecure.security.SecurityConfig;
import com.aerosecure.service.UserService;
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

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Positive Test Case ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createEngineer_AsAdmin_Success() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("engineer1")
                .password("password123")
                .build();

        UserResponseDTO response = UserResponseDTO.builder()
                .id(1L)
                .username("engineer1")
                .role("ROLE_ENGINEER")
                .build();

        when(userService.createEngineer(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/engineers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Engineer created successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("engineer1"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ENGINEER"));
    }

    // --- Negative Test Cases ---

    @Test
    @WithMockUser(roles = "ENGINEER")
    public void createEngineer_AsEngineer_Forbidden() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("engineer1")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/engineers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createEngineer_InvalidPayload_BadRequest() throws Exception {
        // Missing username and password
        CreateUserRequest request = CreateUserRequest.builder()
                .username("")
                .password("")
                .build();

        mockMvc.perform(post("/api/users/engineers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.username").value("Username is required"))
                .andExpect(jsonPath("$.data.password").value("Password is required"));
    }
}
