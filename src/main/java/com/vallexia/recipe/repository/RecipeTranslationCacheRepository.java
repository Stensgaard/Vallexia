package com.vallexia.recipe.repository;

import com.vallexia.recipe.entity.RecipeTranslationCache;
import com.vallexia.recipe.entity.RecipeTranslationCacheId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RecipeTranslationCache entity operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Repository
public interface RecipeTranslationCacheRepository 
        extends JpaRepository<RecipeTranslationCache, RecipeTranslationCacheId> {
    
    /**
     * Find translation cache by Spoonacular ID and locale.
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @param locale the locale
     * @return optional translation cache entry
     */
    Optional<RecipeTranslationCache> findBySpoonacularIdAndLocale(Integer spoonacularId, String locale);
    
    /**
     * Find translation cache by Spoonacular ID and locale that is not expired.
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @param locale the locale
     * @param now current timestamp
     * @return optional non-expired translation cache entry
     */
    @Query("SELECT rtc FROM RecipeTranslationCache rtc " +
           "WHERE rtc.spoonacularId = :spoonacularId " +
           "AND rtc.locale = :locale " +
           "AND rtc.expiresAt > :now")
    Optional<RecipeTranslationCache> findBySpoonacularIdAndLocaleAndNotExpired(
            @Param("spoonacularId") Integer spoonacularId,
            @Param("locale") String locale,
            @Param("now") LocalDateTime now);
    
    /**
     * Find all expired translation cache entries.
     * 
     * @param now current timestamp
     * @return list of expired translation cache entries
     */
    @Query("SELECT rtc FROM RecipeTranslationCache rtc WHERE rtc.expiresAt <= :now")
    List<RecipeTranslationCache> findExpiredEntries(@Param("now") LocalDateTime now);
    
    /**
     * Delete all expired translation cache entries.
     * 
     * @param now current timestamp
     * @return number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM RecipeTranslationCache rtc WHERE rtc.expiresAt <= :now")
    int deleteExpiredEntries(@Param("now") LocalDateTime now);
    
    /**
     * Delete all translations for a specific recipe (cascade delete when recipe cache expires).
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @return number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM RecipeTranslationCache rtc WHERE rtc.spoonacularId = :spoonacularId")
    int deleteBySpoonacularId(@Param("spoonacularId") Integer spoonacularId);
}
