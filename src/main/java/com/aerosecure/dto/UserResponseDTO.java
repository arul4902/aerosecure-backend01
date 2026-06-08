package com.aerosecure.dto;

import lombok.*;

/**
 * Data Transfer Object for User responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String role;
}
