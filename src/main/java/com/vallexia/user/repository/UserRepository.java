package com.vallexia.user.repository;

import com.vallexia.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Check if username exists.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists.
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find enabled user by username.
     * 
     * @param username the username to search for
     * @return Optional containing the enabled user if found
     */
    Optional<User> findByUsernameAndEnabledTrue(String username);
    
    /**
     * Find enabled user by email.
     * 
     * @param email the email to search for
     * @return Optional containing the enabled user if found
     */
    Optional<User> findByEmailAndEnabledTrue(String email);
}
