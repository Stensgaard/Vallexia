package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Ingredient entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
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
}
