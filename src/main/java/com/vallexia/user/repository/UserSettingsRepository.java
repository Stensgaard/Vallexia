package com.vallexia.user.repository;

import com.vallexia.user.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for UserSettings entity operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
    
    /**
     * Check if user settings exist for user ID.
     * 
     * @param userId the user ID to check
     * @return true if settings exist, false otherwise
     */
    boolean existsByUserId(Long userId);
}
