package com.vallexia.user.repository;

import com.vallexia.user.entity.NutritionalGoals;
import com.vallexia.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for NutritionalGoals entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
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
}
