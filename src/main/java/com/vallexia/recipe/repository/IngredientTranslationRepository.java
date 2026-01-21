package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.IngredientTranslation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("""
        select t
        from IngredientTranslation t
        join fetch t.ingredient i
        where t.locale = :locale
          and lower(t.name) = lower(:name)
        """)
    Optional<IngredientTranslation> findByLocaleAndNameIgnoreCase(
        @Param("locale") String locale, @Param("name") String name);

    @Query("""
        select t
        from IngredientTranslation t
        join fetch t.ingredient i
        where t.locale = :locale
          and lower(t.name) like concat('%', lower(:needle), '%')
        """)
    List<IngredientTranslation> findByLocaleAndNameContainsIgnoreCase(
        @Param("locale") String locale, @Param("needle") String needle);
}
