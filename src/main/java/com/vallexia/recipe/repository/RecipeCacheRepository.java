package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.RecipeCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for RecipeCache entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Repository
public interface RecipeCacheRepository extends JpaRepository<RecipeCache, Integer> {
    
    /**
     * Find recipe cache by search hash.
     * 
     * @param searchHash the search hash
     * @return list of cached recipes for this search
     */
    List<RecipeCache> findBySearchHash(String searchHash);
    
    /**
     * Find recipe cache by search hash that are not expired.
     * 
     * @param searchHash the search hash
     * @param now current timestamp
     * @return list of non-expired cached recipes for this search
     */
    @Query("SELECT rc FROM RecipeCache rc WHERE rc.searchHash = :searchHash AND rc.expiresAt > :now")
    List<RecipeCache> findBySearchHashAndNotExpired(@Param("searchHash") String searchHash, 
                                                     @Param("now") LocalDateTime now);
    
    /**
     * Find all expired cache entries.
     * 
     * @param now current timestamp
     * @return list of expired cache entries
     */
    @Query("SELECT rc FROM RecipeCache rc WHERE rc.expiresAt <= :now")
    List<RecipeCache> findExpiredEntries(@Param("now") LocalDateTime now);
    
    /**
     * Delete all expired cache entries.
     * 
     * @param now current timestamp
     * @return number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM RecipeCache rc WHERE rc.expiresAt <= :now")
    int deleteExpiredEntries(@Param("now") LocalDateTime now);
    
    /**
     * Find recipes matching search criteria in cache.
     * This uses PostgreSQL array operations for filtering.
     * 
     * @param cuisines list of cuisines to match (any)
     * @param diets list of diets to match (any)
     * @param intolerances list of intolerances to match (any)
     * @param ingredients list of ingredients to match (any)
     * @param now current timestamp
     * @return list of matching cached recipes
     */
    @Query(value = """
        SELECT * FROM recipe_cache rc
        WHERE rc.expires_at > :now
        AND (array_length(:cuisines, 1) IS NULL OR rc.cuisine && :cuisines)
        AND (array_length(:diets, 1) IS NULL OR rc.diets && :diets)
        AND (array_length(:intolerances, 1) IS NULL OR rc.intolerances && :intolerances)
        AND (array_length(:ingredients, 1) IS NULL OR rc.ingredients && :ingredients)
        """, nativeQuery = true)
    List<RecipeCache> findMatchingRecipes(@Param("cuisines") String[] cuisines,
                                          @Param("diets") String[] diets,
                                          @Param("intolerances") String[] intolerances,
                                          @Param("ingredients") String[] ingredients,
                                          @Param("now") LocalDateTime now);
}
