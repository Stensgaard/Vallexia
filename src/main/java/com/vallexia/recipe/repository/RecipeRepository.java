package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.recipe.entity.enums.RecipeCategory;
import com.vallexia.user.entity.enums.CuisineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Recipe entity operations with advanced search capabilities.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {
    
    /**
     * Find all public recipes.
     * 
     * @param pageable pagination information
     * @return Page of public recipes
     */
    Page<Recipe> findByIsPublicTrue(Pageable pageable);
    
    /**
     * Find recipes by name containing the given string (case-insensitive).
     * 
     * @param name the partial name to search for
     * @param pageable pagination information
     * @return Page of matching recipes
     */
    Page<Recipe> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find recipes by category.
     * 
     * @param category the recipe category
     * @param pageable pagination information
     * @return Page of recipes
     */
    Page<Recipe> findByCategory(RecipeCategory category, Pageable pageable);
    
    /**
     * Find recipes by cuisine type.
     * 
     * @param cuisineType the cuisine type
     * @param pageable pagination information
     * @return Page of recipes
     */
    Page<Recipe> findByCuisineType(CuisineType cuisineType, Pageable pageable);
    
    /**
     * Find recipes by difficulty level.
     * 
     * @param difficultyLevel the difficulty level
     * @param pageable pagination information
     * @return Page of recipes
     */
    Page<Recipe> findByDifficultyLevel(DifficultyLevel difficultyLevel, Pageable pageable);
    
    /**
     * Find public recipes by category.
     * 
     * @param category the recipe category
     * @param pageable pagination information
     * @return Page of public recipes
     */
    Page<Recipe> findByCategoryAndIsPublicTrue(RecipeCategory category, Pageable pageable);
    
    /**
     * Find public recipes by cuisine type.
     * 
     * @param cuisineType the cuisine type
     * @param pageable pagination information
     * @return Page of public recipes
     */
    Page<Recipe> findByCuisineTypeAndIsPublicTrue(CuisineType cuisineType, Pageable pageable);
    
    /**
     * Full-text search on recipe name and description.
     * Uses PostgreSQL full-text search capabilities.
     * 
     * @param searchText the text to search for
     * @param pageable pagination information
     * @return Page of matching recipes
     */
    @Query(value = "SELECT r.* FROM recipes r WHERE " +
           "to_tsvector('english', r.name || ' ' || COALESCE(r.description, '')) @@ plainto_tsquery('english', :searchText) " +
           "AND r.is_public = true",
           countQuery = "SELECT COUNT(r.*) FROM recipes r WHERE " +
           "to_tsvector('english', r.name || ' ' || COALESCE(r.description, '')) @@ plainto_tsquery('english', :searchText) " +
           "AND r.is_public = true",
           nativeQuery = true)
    Page<Recipe> fullTextSearch(@Param("searchText") String searchText, Pageable pageable);
    
    /**
     * Find recipes containing a specific ingredient.
     * 
     * @param ingredientName the ingredient name
     * @param pageable pagination information
     * @return Page of recipes containing the ingredient
     */
    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.ingredients ri JOIN ri.ingredient i " +
           "WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredientName, '%')) " +
           "AND r.isPublic = true")
    Page<Recipe> findByIngredientNameContaining(@Param("ingredientName") String ingredientName, Pageable pageable);
    
    /**
     * Find recent recipes ordered by creation date.
     * 
     * @param pageable pagination information
     * @return Page of recent public recipes
     */
    Page<Recipe> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
}
