package com.vallexia.recipe.service.specification;

import com.vallexia.recipe.dto.RecipeSearchCriteria;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Builder for creating JPA Specifications from recipe search criteria.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
public final class RecipeSpecificationBuilder {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private RecipeSpecificationBuilder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Build JPA Specification from search criteria.
     * 
     * @param criteria search criteria
     * @param userAllergies user's allergies (for auto-hiding recipes, can be null or empty)
     * @param preferredCuisines user's preferred cuisines (for automatic filtering, can be null or empty)
     * @return JPA Specification
     */
    public static Specification<Recipe> buildSpecification(
            RecipeSearchCriteria criteria, List<SupportedAllergy> userAllergies, Set<SupportedCuisineType> preferredCuisines) {
        Specification<Recipe> spec = (root, query, cb) -> cb.conjunction();
        
        // Text search on name and description
        if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
            spec = spec.and(textSearch(criteria.getQuery().trim()));
        }
        
        // Category filter
        if (criteria.getCategory() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), criteria.getCategory()));
        }
        
        // Cuisine type filter - mutually exclusive: either explicit filter OR preferred cuisines
        if (criteria.getCuisineType() != null) {
            // Explicit cuisine filter takes precedence - ignore preferred cuisines
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cuisineType"), criteria.getCuisineType()));
        } else if (preferredCuisines != null && !preferredCuisines.isEmpty()) {
            // Use preferred cuisines when no explicit filter is provided
            spec = spec.and(preferredCuisinesFilter(preferredCuisines));
        }
        
        // Difficulty level filter
        if (criteria.getDifficultyLevel() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("difficultyLevel"), criteria.getDifficultyLevel()));
        }
        
        // Prep time filters
        if (criteria.getMinPrepTime() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.greaterThanOrEqualTo(root.get("prepTimeMinutes"), criteria.getMinPrepTime()));
        }
        if (criteria.getMaxPrepTime() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.lessThanOrEqualTo(root.get("prepTimeMinutes"), criteria.getMaxPrepTime()));
        }
        
        // Cook time filters
        if (criteria.getMinCookTime() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.greaterThanOrEqualTo(root.get("cookTimeMinutes"), criteria.getMinCookTime()));
        }
        if (criteria.getMaxCookTime() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.lessThanOrEqualTo(root.get("cookTimeMinutes"), criteria.getMaxCookTime()));
        }
        
        // Total time filters
        if (criteria.getMinTotalTime() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.greaterThanOrEqualTo(root.get("totalTimeMinutes"), criteria.getMinTotalTime()));
        }
        if (criteria.getMaxTotalTime() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.lessThanOrEqualTo(root.get("totalTimeMinutes"), criteria.getMaxTotalTime()));
        }
        
        // Servings filters
        if (criteria.getMinServings() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.greaterThanOrEqualTo(root.get("servings"), criteria.getMinServings()));
        }
        if (criteria.getMaxServings() != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.lessThanOrEqualTo(root.get("servings"), criteria.getMaxServings()));
        }
        
        // Calories filter (from nutritional info)
        if (criteria.getMinCalories() != null || criteria.getMaxCalories() != null) {
            spec = spec.and(caloriesFilter(criteria.getMinCalories(), criteria.getMaxCalories()));
        }
        
        // Dietary restrictions filter
        if (criteria.getDietaryRestrictions() != null && !criteria.getDietaryRestrictions().isEmpty()) {
            spec = spec.and(DietaryRestrictionFilter.filter(
                    criteria.getDietaryRestrictions(), criteria.getRestrictionMatchMode()));
        }
        
        // Allergen filter (auto-hide recipes containing user's allergies)
        if (criteria.getExcludeAllergens() != null && criteria.getExcludeAllergens() 
                && userAllergies != null && !userAllergies.isEmpty()) {
            spec = spec.and(allergenFilter(userAllergies));
        }
        
        // Only show public recipes in search results
        // Note: Recipe creation/editing/deletion is restricted to admins only.
        // Regular users can only read and favorite recipes.
        spec = spec.and((root, query, cb) -> cb.equal(root.get("isPublic"), true));
        
        return spec;
    }
    
    /**
     * Text search specification for name and description.
     * 
     * @param query search query string
     * @return Specification for text search
     */
    private static Specification<Recipe> textSearch(String query) {
        return (root, queryBuilder, cb) -> {
            String searchPattern = "%" + query.toLowerCase() + "%";
            Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchPattern);
            Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
            return cb.or(nameMatch, descMatch);
        };
    }
    
    /**
     * Calories filter specification.
     * 
     * @param minCalories minimum calories (can be null)
     * @param maxCalories maximum calories (can be null)
     * @return Specification for calories filter
     */
    private static Specification<Recipe> caloriesFilter(BigDecimal minCalories, BigDecimal maxCalories) {
        return (root, queryBuilder, cb) -> {
            Join<Recipe, com.vallexia.recipe.entity.NutritionalInfo> nutritionJoin = 
                    root.join("nutritionalInfo", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();
            
            if (minCalories != null) {
                predicates.add(cb.greaterThanOrEqualTo(nutritionJoin.get("calories"), minCalories));
            }
            if (maxCalories != null) {
                predicates.add(cb.lessThanOrEqualTo(nutritionJoin.get("calories"), maxCalories));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Allergen filter specification.
     * Excludes recipes that contain any of the user's allergies.
     * 
     * Performance: Uses EXISTS subquery which is efficient for ElementCollection filtering.
     * 
     * @param userAllergies list of user's allergies
     * @return Specification that excludes recipes with matching allergens
     */
    private static Specification<Recipe> allergenFilter(List<SupportedAllergy> userAllergies) {
        return (root, query, cb) -> {
            // Note: query is always provided by JPA Specifications framework (linter warnings are false positives)
            // Exclude recipes that have ANY of the user's allergies
            // Use EXISTS subquery to check if recipe has any matching allergen
            // For @ElementCollection of enums, the join path IS the enum value
            if (query == null) {
                return cb.disjunction(); // Return false predicate if query is null (should never happen)
            }
            var subquery = query.subquery(Long.class);
            var subRoot = subquery.from(Recipe.class);
            var subJoin = subRoot.join("allergens", JoinType.INNER);
            subquery.select(subRoot.get("id"))
                    .where(cb.and(
                        cb.equal(subRoot.get("id"), root.get("id")),
                        subJoin.in(userAllergies)
                    ));
            
            // Return recipes that do NOT exist in the subquery (no matching allergens)
            return cb.not(cb.exists(subquery));
        };
    }
    
    /**
     * Preferred cuisines filter specification.
     * Filters recipes to match any of the user's preferred cuisines (OR logic).
     * 
     * @param preferredCuisines set of user's preferred cuisine types
     * @return Specification that filters recipes by preferred cuisines
     */
    private static Specification<Recipe> preferredCuisinesFilter(Set<SupportedCuisineType> preferredCuisines) {
        return (root, query, cb) -> {
            // Recipe matches if it has ANY of the preferred cuisines
            return root.get("cuisineType").in(preferredCuisines);
        };
    }
}
