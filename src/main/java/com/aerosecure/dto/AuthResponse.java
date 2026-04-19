package com.aerosecure.dto;

import lombok.*;

/**
 * Response DTO returned after successful authentication.
 * Contains the JWT token and user information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String role;
}
