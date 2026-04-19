package com.aerosecure.service;

import com.aerosecure.dto.AircraftDTO;
import com.aerosecure.entity.Aircraft;
import com.aerosecure.entity.AircraftStatus;
import com.aerosecure.exception.BadRequestException;
import com.aerosecure.exception.ResourceNotFoundException;
import com.aerosecure.repository.AircraftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AircraftService.
 * Contains all business logic for aircraft fleet management.
 */
@Service
@Transactional
public class AircraftServiceImpl implements AircraftService {

    private static final Logger logger = LoggerFactory.getLogger(AircraftServiceImpl.class);

    @Autowired
    private AircraftRepository aircraftRepository;

    @Override
    public AircraftDTO createAircraft(AircraftDTO aircraftDTO) {
        logger.info("Creating new aircraft: {} by {}", aircraftDTO.getModel(), aircraftDTO.getManufacturer());

        Aircraft aircraft = mapToEntity(aircraftDTO);
        Aircraft savedAircraft = aircraftRepository.save(aircraft);

        logger.info("Aircraft created successfully with ID: {}", savedAircraft.getAircraftId());
        return mapToDTO(savedAircraft);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AircraftDTO> getAllAircraft(Pageable pageable) {
        logger.info("Fetching all aircraft with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Aircraft> aircraftPage = aircraftRepository.findAll(pageable);
        return aircraftPage.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AircraftDTO getAircraftById(Long id) {
        logger.info("Fetching aircraft with ID: {}", id);

        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft", "aircraftId", id));

        return mapToDTO(aircraft);
    }

    @Override
    public AircraftDTO updateAircraft(Long id, AircraftDTO aircraftDTO) {
        logger.info("Updating aircraft with ID: {}", id);

        Aircraft existingAircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft", "aircraftId", id));

        existingAircraft.setModel(aircraftDTO.getModel());
        existingAircraft.setManufacturer(aircraftDTO.getManufacturer());
        existingAircraft.setStatus(aircraftDTO.getStatus());

        Aircraft updatedAircraft = aircraftRepository.save(existingAircraft);

        logger.info("Aircraft updated successfully: ID={}", updatedAircraft.getAircraftId());
        return mapToDTO(updatedAircraft);
    }

    @Override
    public void deleteAircraft(Long id) {
        logger.info("Deleting aircraft with ID: {}", id);

        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft", "aircraftId", id));

        aircraftRepository.delete(aircraft);
        logger.info("Aircraft deleted successfully: ID={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AircraftDTO> getAircraftByStatus(AircraftStatus status) {
        logger.info("Filtering aircraft by status: {}", status);

        List<Aircraft> aircraftList = aircraftRepository.findByStatus(status);

        if (aircraftList.isEmpty()) {
            logger.info("No aircraft found with status: {}", status);
        }

        return aircraftList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AircraftDTO> searchByManufacturer(String manufacturer) {
        logger.info("Searching aircraft by manufacturer: {}", manufacturer);

        if (manufacturer == null || manufacturer.trim().isEmpty()) {
            throw new BadRequestException("Manufacturer search term cannot be empty");
        }

        List<Aircraft> aircraftList = aircraftRepository.findByManufacturerContainingIgnoreCase(manufacturer);
        return aircraftList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Mapping Helpers ====================

    /**
     * Convert Aircraft entity to AircraftDTO.
     */
    private AircraftDTO mapToDTO(Aircraft aircraft) {
        return AircraftDTO.builder()
                .aircraftId(aircraft.getAircraftId())
                .model(aircraft.getModel())
                .manufacturer(aircraft.getManufacturer())
                .status(aircraft.getStatus())
                .createdAt(aircraft.getCreatedAt())
                .updatedAt(aircraft.getUpdatedAt())
                .build();
    }

    /**
     * Convert AircraftDTO to Aircraft entity.
     */
    private Aircraft mapToEntity(AircraftDTO dto) {
        return Aircraft.builder()
                .model(dto.getModel())
                .manufacturer(dto.getManufacturer())
                .status(dto.getStatus())
                .build();
    }
}
