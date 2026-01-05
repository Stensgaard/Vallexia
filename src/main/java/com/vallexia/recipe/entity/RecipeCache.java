package com.vallexia.recipe.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity for caching Spoonacular recipes in the database.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Entity
@Table(name = "recipe_cache", indexes = {
    @Index(name = "idx_recipe_cache_search_hash", columnList = "search_hash"),
    @Index(name = "idx_recipe_cache_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeCache {
    
    @Id
    @Column(name = "spoonacular_id")
    private Integer spoonacularId;
    
    @Column(name = "search_hash", length = 64)
    private String searchHash;
    
    @Column(name = "recipe_data", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String recipeData;
    
    @Column(name = "recipe_name", length = 255)
    private String recipeName;
    
    @Column(name = "cuisine", columnDefinition = "varchar(50)[]")
    private String[] cuisine;
    
    @Column(name = "diets", columnDefinition = "varchar(50)[]")
    private String[] diets;
    
    @Column(name = "intolerances", columnDefinition = "varchar(50)[]")
    private String[] intolerances;
    
    @Column(name = "ingredients", columnDefinition = "varchar(255)[]")
    private String[] ingredients;
    
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
