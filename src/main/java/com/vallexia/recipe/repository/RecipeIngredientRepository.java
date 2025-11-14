package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for RecipeIngredient entity operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    
    /**
     * Find all recipe ingredients for a recipe, ordered by display order.
     * 
     * @param recipeId the recipe ID
     * @return List of recipe ingredients ordered by displayOrder
     */
    @Query("SELECT ri FROM RecipeIngredient ri WHERE ri.recipe.id = :recipeId ORDER BY ri.displayOrder ASC")
    List<RecipeIngredient> findByRecipeId(@Param("recipeId") Long recipeId);
    
    /**
     * Delete all recipe ingredients for a recipe.
     * 
     * @param recipeId the recipe ID
     */
    void deleteByRecipeId(Long recipeId);
}
