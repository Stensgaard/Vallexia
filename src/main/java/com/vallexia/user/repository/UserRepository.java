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
    
    /**
     * Find user by username (regardless of enabled status).
     * Username is immutable and preferred for account status checks.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email (regardless of enabled status).
     * Note: Email can be changed by users, so username lookup is preferred when possible.
     * This method is needed to support login with email address.
     * 
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
}
