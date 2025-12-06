package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.RecipeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for RecipeTranslation entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Repository
public interface RecipeTranslationRepository extends JpaRepository<RecipeTranslation, Long> {
    
    /**
     * Find translation for a recipe by locale.
     * 
     * @param recipeId the recipe ID
     * @param locale the locale
     * @return Optional containing the translation if found
     */
    Optional<RecipeTranslation> findByRecipeIdAndLocale(Long recipeId, String locale);
}
