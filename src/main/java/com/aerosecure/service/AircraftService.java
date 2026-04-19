package com.aerosecure.service;

import com.aerosecure.dto.AircraftDTO;
import com.aerosecure.entity.AircraftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Aircraft business operations.
 */
public interface AircraftService {

    /**
     * Create a new aircraft record.
     */
    AircraftDTO createAircraft(AircraftDTO aircraftDTO);

    /**
     * Retrieve all aircraft with pagination and sorting.
     */
    Page<AircraftDTO> getAllAircraft(Pageable pageable);

    /**
     * Retrieve a single aircraft by its ID.
     */
    AircraftDTO getAircraftById(Long id);

    /**
     * Update an existing aircraft record.
     */
    AircraftDTO updateAircraft(Long id, AircraftDTO aircraftDTO);

    /**
     * Delete an aircraft record by its ID.
     */
    void deleteAircraft(Long id);

    /**
     * Filter aircraft by operational status.
     */
    List<AircraftDTO> getAircraftByStatus(AircraftStatus status);

    /**
     * Search aircraft by manufacturer name (case-insensitive).
     */
    List<AircraftDTO> searchByManufacturer(String manufacturer);
}
