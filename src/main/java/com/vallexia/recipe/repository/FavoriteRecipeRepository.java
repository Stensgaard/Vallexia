package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.FavoriteRecipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for FavoriteRecipe entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Repository
public interface FavoriteRecipeRepository extends JpaRepository<FavoriteRecipe, Long> {
    
    /**
     * Find favorite recipes for a user with pagination.
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return Page of favorite recipes
     */
    Page<FavoriteRecipe> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Check if recipe is favorited by user.
     * 
     * @param userId the user ID
     * @param recipeId the recipe ID
     * @return true if favorited, false otherwise
     */
    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);
    
    /**
     * Delete favorite recipe by user and recipe IDs.
     * 
     * @param userId the user ID
     * @param recipeId the recipe ID
     */
    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);
}
