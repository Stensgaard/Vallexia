package com.vallexia.recipe.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for RecipeTranslationCache entity.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeTranslationCacheId implements Serializable {
    
    private Integer spoonacularId;
    private String locale;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeTranslationCacheId that = (RecipeTranslationCacheId) o;
        return Objects.equals(spoonacularId, that.spoonacularId) &&
               Objects.equals(locale, that.locale);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(spoonacularId, locale);
    }
}
