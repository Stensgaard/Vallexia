package com.vallexia.user.repository;

import com.vallexia.user.entity.DietaryPreferences;
import com.vallexia.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for DietaryPreferences entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
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
}
