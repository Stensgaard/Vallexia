package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.IngredientNutrition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for IngredientNutrition entity.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface IngredientNutritionRepository extends JpaRepository<IngredientNutrition, Long> {
    
    /**
     * Find nutritional information by ingredient ID.
     * 
     * @param ingredientId the ingredient ID
     * @return optional ingredient nutrition
     */
    Optional<IngredientNutrition> findByIngredientId(Long ingredientId);
    
    /**
     * Check if nutritional information exists for an ingredient.
     * 
     * @param ingredientId the ingredient ID
     * @return true if nutrition data exists
     */
    boolean existsByIngredientId(Long ingredientId);
}
