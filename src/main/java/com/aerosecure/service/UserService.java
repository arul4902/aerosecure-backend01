package com.aerosecure.service;

import com.aerosecure.dto.CreateUserRequest;
import com.aerosecure.dto.UserResponseDTO;

/**
 * Service interface for User/Engineer management operations.
 */
public interface UserService {

    /**
     * Create a new engineer user in the system.
     *
     * @param request the details of the engineer to create
     * @return the details of the created engineer
     */
    UserResponseDTO createEngineer(CreateUserRequest request);
}
