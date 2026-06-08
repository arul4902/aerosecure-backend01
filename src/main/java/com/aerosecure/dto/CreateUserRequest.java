package com.aerosecure.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for creating a new user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
