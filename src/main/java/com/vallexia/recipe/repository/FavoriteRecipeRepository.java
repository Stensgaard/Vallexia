package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.FavoriteRecipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for FavoriteRecipe entity operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
     * Find favorite recipe by user and recipe IDs.
     * 
     * @param userId the user ID
     * @param recipeId the recipe ID
     * @return Optional containing the favorite recipe if found
     */
    Optional<FavoriteRecipe> findByUserIdAndRecipeId(Long userId, Long recipeId);
    
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
    
    /**
     * Count favorites for a recipe.
     * 
     * @param recipeId the recipe ID
     * @return number of users who favorited the recipe
     */
    @Query("SELECT COUNT(fr) FROM FavoriteRecipe fr WHERE fr.recipe.id = :recipeId")
    long countByRecipeId(@Param("recipeId") Long recipeId);
}
