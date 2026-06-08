package com.aerosecure.controller;

import com.aerosecure.dto.ApiResponse;
import com.aerosecure.dto.CreateUserRequest;
import com.aerosecure.dto.UserResponseDTO;
import com.aerosecure.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for User Management.
 * Provides endpoints for administrative tasks related to system users.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for User administration and management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Create a new engineer user.
     * POST /api/users/engineers
     * Access: ADMIN only
     */
    @PostMapping("/engineers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new engineer user", description = "Register a new engineer user in the system (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createEngineer(@Valid @RequestBody CreateUserRequest request) {
        logger.info("POST /api/users/engineers - Admin creating new engineer: {}", request.getUsername());
        UserResponseDTO created = userService.createEngineer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Engineer created successfully", created));
    }
}
