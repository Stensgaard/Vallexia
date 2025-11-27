package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for RecipeIngredient entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    
    /**
     * Delete all recipe ingredients for a recipe.
     * 
     * @param recipeId the recipe ID
     */
    void deleteByRecipeId(Long recipeId);
}
