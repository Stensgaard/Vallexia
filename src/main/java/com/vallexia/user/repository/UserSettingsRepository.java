package com.vallexia.user.repository;

import com.vallexia.user.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for UserSettings entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    
    /**
     * Find user settings by user ID.
     * 
     * @param userId the user ID to search for
     * @return Optional containing the user settings if found
     */
    Optional<UserSettings> findByUserId(Long userId);
}
