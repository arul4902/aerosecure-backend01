package com.aerosecure.repository;

import com.aerosecure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user with the given username exists.
     */
    boolean existsByUsername(String username);
}
