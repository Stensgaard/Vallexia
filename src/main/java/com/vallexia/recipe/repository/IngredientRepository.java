package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Ingredient entity operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    
    /**
     * Find ingredient by exact name (case-insensitive).
     * 
     * @param name the ingredient name to search for
     * @return Optional containing the ingredient if found
     */
    Optional<Ingredient> findByNameIgnoreCase(String name);
    
    /**
     * Find ingredients by name containing the given string (case-insensitive).
     * Useful for autocomplete functionality.
     * 
     * @param name the partial name to search for
     * @return List of matching ingredients
     */
    List<Ingredient> findByNameContainingIgnoreCase(String name);
    
    /**
     * Check if ingredient exists by name (case-insensitive).
     * 
     * @param name the ingredient name to check
     * @return true if ingredient exists, false otherwise
     */
    @Query("SELECT COUNT(i) > 0 FROM Ingredient i WHERE LOWER(i.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);
}
