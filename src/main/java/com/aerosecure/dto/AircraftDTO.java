package com.aerosecure.dto;

import com.aerosecure.entity.AircraftStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Aircraft entity.
 * Used for request validation and response serialization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AircraftDTO {

    private Long aircraftId;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotNull(message = "Status is required")
    private AircraftStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
