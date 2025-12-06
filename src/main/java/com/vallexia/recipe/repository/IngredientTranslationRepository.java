package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.IngredientTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for IngredientTranslation entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
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
}
