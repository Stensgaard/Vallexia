package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.RecipeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RecipeTranslation entity operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
    
    /**
     * Find all translations for a recipe.
     * 
     * @param recipeId the recipe ID
     * @return List of translations
     */
    List<RecipeTranslation> findByRecipeId(Long recipeId);
    
    /**
     * Delete all translations for a recipe.
     * 
     * @param recipeId the recipe ID
     */
    void deleteByRecipeId(Long recipeId);
}
