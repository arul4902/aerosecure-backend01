package com.aerosecure.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for user authentication (login).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
