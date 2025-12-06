package com.vallexia.nutrition.repository;

import com.vallexia.nutrition.entity.NutritionalGoals;
import com.vallexia.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for nutritional goals operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-27
 */
@Repository
public interface NutritionalGoalsRepository extends JpaRepository<NutritionalGoals, Long> {
    
    /**
     * Find nutritional goals by user.
     * 
     * @param user the user
     * @return optional nutritional goals
     */
    Optional<NutritionalGoals> findByUser(User user);
    
    /**
     * Find nutritional goals by user ID.
     * 
     * @param userId the user ID
     * @return optional nutritional goals
     */
    Optional<NutritionalGoals> findByUserId(Long userId);
    
    /**
     * Check if nutritional goals exist for a user.
     * 
     * @param user the user
     * @return true if goals exist
     */
    boolean existsByUser(User user);
}
