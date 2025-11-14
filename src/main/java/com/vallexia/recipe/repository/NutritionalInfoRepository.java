package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.NutritionalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for NutritionalInfo entity operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface NutritionalInfoRepository extends JpaRepository<NutritionalInfo, Long> {
    
    /**
     * Find nutritional info by recipe ID.
     * 
     * @param recipeId the recipe ID
     * @return Optional containing the nutritional info if found
     */
    Optional<NutritionalInfo> findByRecipeId(Long recipeId);
    
    /**
     * Check if nutritional info exists for recipe.
     * 
     * @param recipeId the recipe ID
     * @return true if nutritional info exists, false otherwise
     */
    boolean existsByRecipeId(Long recipeId);
    
    /**
     * Delete nutritional info by recipe ID.
     * 
     * @param recipeId the recipe ID
     */
    void deleteByRecipeId(Long recipeId);
}
