package com.aerosecure.controller;

import com.aerosecure.dto.ApiResponse;
import com.aerosecure.dto.AuthRequest;
import com.aerosecure.dto.AuthResponse;
import com.aerosecure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication (login) operations.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for User Authentication")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate user and return JWT token.
     * POST /api/auth/login
     * Access: PUBLIC
     */
    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate and receive a JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        logger.info("POST /api/auth/login - Authenticating user: {}", authRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("ROLE_ENGINEER");

        AuthResponse authResponse = AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .username(authRequest.getUsername())
                .role(role)
                .build();

        logger.info("User '{}' authenticated successfully with role: {}", authRequest.getUsername(), role);

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
}
