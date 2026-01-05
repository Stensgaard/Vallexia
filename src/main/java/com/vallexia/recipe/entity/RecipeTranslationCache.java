package com.vallexia.recipe.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity for caching translated recipe content in the database.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Entity
@Table(name = "recipe_translation_cache", 
       indexes = {
           @Index(name = "idx_recipe_translation_cache_expires_at", columnList = "expires_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RecipeTranslationCacheId.class)
public class RecipeTranslationCache {
    
    @Id
    @Column(name = "spoonacular_id", nullable = false)
    private Integer spoonacularId;
    
    @Id
    @Column(name = "locale", length = 10, nullable = false)
    private String locale;
    
    @Column(name = "translated_name", length = 255)
    private String translatedName;
    
    @Column(name = "translated_description", columnDefinition = "TEXT")
    private String translatedDescription;
    
    @Column(name = "translated_instructions", columnDefinition = "TEXT")
    private String translatedInstructions;
    
    @Column(name = "translated_ingredients", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String translatedIngredients; // JSON array of translated ingredient names
    
    @Column(name = "cached_at", nullable = false, updatable = false)
    private LocalDateTime cachedAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * Check if this cache entry is expired.
     * 
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
