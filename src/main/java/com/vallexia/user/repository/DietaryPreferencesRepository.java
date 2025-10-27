package com.vallexia.user.repository;

import com.vallexia.user.entity.DietaryPreferences;
import com.vallexia.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for DietaryPreferences entity operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface DietaryPreferencesRepository extends JpaRepository<DietaryPreferences, Long> {
    
    /**
     * Find dietary preferences by user.
     * 
     * @param user the user to search for
     * @return Optional containing the dietary preferences if found
     */
    Optional<DietaryPreferences> findByUser(User user);
    
    /**
     * Find dietary preferences by user ID.
     * 
     * @param userId the user ID to search for
     * @return Optional containing the dietary preferences if found
     */
    Optional<DietaryPreferences> findByUserId(Long userId);
    
    /**
     * Check if dietary preferences exist for user.
     * 
     * @param user the user to check
     * @return true if preferences exist, false otherwise
     */
    boolean existsByUser(User user);
    
    /**
     * Check if dietary preferences exist for user ID.
     * 
     * @param userId the user ID to check
     * @return true if preferences exist, false otherwise
     */
    boolean existsByUserId(Long userId);
}
