package com.vallexia.recipe.entity.enums;

/**
 * Match mode for dietary restrictions filtering in recipe search.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
public enum RestrictionMatchMode {
    /**
     * Recipe matches if it has ANY of the requested restrictions (OR logic).
     */
    OR,
    
    /**
     * Recipe matches only if it has ALL requested restrictions (AND logic).
     */
    AND
}
