package com.aerosecure.controller;

import com.aerosecure.dto.AircraftDTO;
import com.aerosecure.dto.ApiResponse;
import com.aerosecure.entity.AircraftStatus;
import com.aerosecure.service.AircraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Aircraft Registration & Fleet Management.
 * Provides CRUD endpoints, filtering, and search capabilities.
 */
@RestController
@RequestMapping("/api/aircraft")
@Tag(name = "Aircraft Management", description = "APIs for Aircraft Registration & Fleet Management")
@SecurityRequirement(name = "bearerAuth")
public class AircraftController {

    private static final Logger logger = LoggerFactory.getLogger(AircraftController.class);

    @Autowired
    private AircraftService aircraftService;

    /**
     * Create a new aircraft record.
     * POST /api/aircraft
     * Access: ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new aircraft", description = "Register a new aircraft in the fleet (ADMIN only)")
    public ResponseEntity<ApiResponse<AircraftDTO>> createAircraft(@Valid @RequestBody AircraftDTO aircraftDTO) {
        logger.info("POST /api/aircraft - Creating new aircraft");
        AircraftDTO created = aircraftService.createAircraft(aircraftDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Aircraft created successfully", created));
    }

    /**
     * Get all aircraft with pagination and sorting.
     * GET /api/aircraft?page=0&size=5&sortBy=model
     * Access: ADMIN, ENGINEER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    @Operation(summary = "Get all aircraft", description = "Retrieve all aircraft with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<AircraftDTO>>> getAllAircraft(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "aircraftId") String sortBy) {
        logger.info("GET /api/aircraft - page={}, size={}, sortBy={}", page, size, sortBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<AircraftDTO> aircraftPage = aircraftService.getAllAircraft(pageable);
        return ResponseEntity.ok(ApiResponse.success("Aircraft retrieved successfully", aircraftPage));
    }

    /**
     * Get a single aircraft by ID.
     * GET /api/aircraft/{id}
     * Access: ADMIN, ENGINEER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    @Operation(summary = "Get aircraft by ID", description = "Retrieve a specific aircraft by its ID")
    public ResponseEntity<ApiResponse<AircraftDTO>> getAircraftById(@PathVariable Long id) {
        logger.info("GET /api/aircraft/{}", id);
        AircraftDTO aircraft = aircraftService.getAircraftById(id);
        return ResponseEntity.ok(ApiResponse.success("Aircraft retrieved successfully", aircraft));
    }

    /**
     * Update an existing aircraft record.
     * PUT /api/aircraft/{id}
     * Access: ADMIN only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update aircraft", description = "Update an existing aircraft record (ADMIN only)")
    public ResponseEntity<ApiResponse<AircraftDTO>> updateAircraft(
            @PathVariable Long id,
            @Valid @RequestBody AircraftDTO aircraftDTO) {
        logger.info("PUT /api/aircraft/{}", id);
        AircraftDTO updated = aircraftService.updateAircraft(id, aircraftDTO);
        return ResponseEntity.ok(ApiResponse.success("Aircraft updated successfully", updated));
    }

    /**
     * Delete an aircraft record.
     * DELETE /api/aircraft/{id}
     * Access: ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete aircraft", description = "Delete an aircraft record by ID (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteAircraft(@PathVariable Long id) {
        logger.info("DELETE /api/aircraft/{}", id);
        aircraftService.deleteAircraft(id);
        return ResponseEntity.ok(ApiResponse.success("Aircraft deleted successfully", null));
    }

    /**
     * Filter aircraft by operational status.
     * GET /api/aircraft/status/{status}
     * Access: ADMIN, ENGINEER
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    @Operation(summary = "Filter by status", description = "Get aircraft filtered by operational status")
    public ResponseEntity<ApiResponse<List<AircraftDTO>>> getAircraftByStatus(@PathVariable AircraftStatus status) {
        logger.info("GET /api/aircraft/status/{}", status);
        List<AircraftDTO> aircraftList = aircraftService.getAircraftByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Aircraft filtered by status: " + status, aircraftList));
    }

    /**
     * Search aircraft by manufacturer name.
     * GET /api/aircraft/search?manufacturer=Boeing
     * Access: ADMIN, ENGINEER
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    @Operation(summary = "Search by manufacturer", description = "Search aircraft by manufacturer name (case-insensitive)")
    public ResponseEntity<ApiResponse<List<AircraftDTO>>> searchByManufacturer(
            @RequestParam String manufacturer) {
        logger.info("GET /api/aircraft/search?manufacturer={}", manufacturer);
        List<AircraftDTO> aircraftList = aircraftService.searchByManufacturer(manufacturer);
        return ResponseEntity.ok(ApiResponse.success("Aircraft search results for: " + manufacturer, aircraftList));
    }
}
