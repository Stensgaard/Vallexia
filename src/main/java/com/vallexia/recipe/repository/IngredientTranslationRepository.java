package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.IngredientTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IngredientTranslation entity operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface IngredientTranslationRepository extends JpaRepository<IngredientTranslation, Long> {
    
    /**
     * Find translation for an ingredient by locale.
     * 
     * @param ingredientId the ingredient ID
     * @param locale the locale
     * @return Optional containing the translation if found
     */
    Optional<IngredientTranslation> findByIngredientIdAndLocale(Long ingredientId, String locale);
    
    /**
     * Find all translations for an ingredient.
     * 
     * @param ingredientId the ingredient ID
     * @return List of translations
     */
    List<IngredientTranslation> findByIngredientId(Long ingredientId);
    
    /**
     * Delete all translations for an ingredient.
     * 
     * @param ingredientId the ingredient ID
     */
    void deleteByIngredientId(Long ingredientId);
}
