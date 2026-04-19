package com.aerosecure.repository;

import com.aerosecure.entity.Aircraft;
import com.aerosecure.entity.AircraftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Aircraft entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {

    /**
     * Find all aircraft by operational status.
     */
    List<Aircraft> findByStatus(AircraftStatus status);

    /**
     * Search aircraft by manufacturer name (case-insensitive partial match).
     */
    List<Aircraft> findByManufacturerContainingIgnoreCase(String manufacturer);

    /**
     * Find all aircraft with pagination support.
     */
    Page<Aircraft> findAll(Pageable pageable);
}
