package com.aerosecure.service;

import com.aerosecure.dto.CreateUserRequest;
import com.aerosecure.dto.UserResponseDTO;
import com.aerosecure.entity.Role;
import com.aerosecure.entity.User;
import com.aerosecure.exception.BadRequestException;
import com.aerosecure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserService interface.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO createEngineer(CreateUserRequest request) {
        logger.info("Creating new engineer: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            logger.error("User registration failed: Username '{}' is already taken", request.getUsername());
            throw new BadRequestException("Username is already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_ENGINEER)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("Engineer user created successfully with ID: {}", savedUser.getId());

        return mapToResponseDTO(savedUser);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
