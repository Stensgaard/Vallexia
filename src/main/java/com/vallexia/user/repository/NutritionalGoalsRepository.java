package com.vallexia.user.repository;

import com.vallexia.user.entity.NutritionalGoals;
import com.vallexia.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for NutritionalGoals entity operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface NutritionalGoalsRepository extends JpaRepository<NutritionalGoals, Long> {
    
    /**
     * Find nutritional goals by user.
     * 
     * @param user the user to search for
     * @return Optional containing the nutritional goals if found
     */
    Optional<NutritionalGoals> findByUser(User user);
    
    /**
     * Find nutritional goals by user ID.
     * 
     * @param userId the user ID to search for
     * @return Optional containing the nutritional goals if found
     */
    Optional<NutritionalGoals> findByUserId(Long userId);
    
    /**
     * Check if nutritional goals exist for user.
     * 
     * @param user the user to check
     * @return true if goals exist, false otherwise
     */
    boolean existsByUser(User user);
    
    /**
     * Check if nutritional goals exist for user ID.
     * 
     * @param userId the user ID to check
     * @return true if goals exist, false otherwise
     */
    boolean existsByUserId(Long userId);
}
