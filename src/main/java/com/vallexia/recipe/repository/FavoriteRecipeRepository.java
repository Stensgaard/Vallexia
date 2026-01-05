package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.FavoriteRecipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * @param spoonacularId the Spoonacular recipe ID
     * @return true if favorited, false otherwise
     */
    boolean existsByUserIdAndSpoonacularId(Long userId, Integer spoonacularId);
    
    /**
     * Delete favorite recipe by user and Spoonacular recipe IDs.
     * 
     * @param userId the user ID
     * @param spoonacularId the Spoonacular recipe ID
     */
    void deleteByUserIdAndSpoonacularId(Long userId, Integer spoonacularId);
    
    /**
     * Find all favorite recipes by Spoonacular IDs.
     * 
     * @param spoonacularIds list of Spoonacular recipe IDs
     * @return list of favorite recipes
     */
    List<FavoriteRecipe> findBySpoonacularIdIn(List<Integer> spoonacularIds);
}
