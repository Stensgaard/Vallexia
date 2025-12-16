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

// TODO remove this class

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
        Specification<Recipe> spec = Specification.where(alwaysTrue());
        
        // Text search on name and description
        String query = criteria.getQuery();
        if (query != null && !query.trim().isEmpty()) {
            spec = spec.and(textSearch(query.trim()));
        }
        spec = andIf(criteria.getCategory() != null, spec, equalsSpec("category", criteria.getCategory()));
        spec = andIf(criteria.getDifficultyLevel() != null, spec, equalsSpec("difficultyLevel", criteria.getDifficultyLevel()));
        
        // Cuisine type filter - mutually exclusive: either explicit filter OR preferred cuisines
        if (criteria.getCuisineType() != null) {
            spec = spec.and(equalsSpec("cuisineType", criteria.getCuisineType()));
        } else if (preferredCuisines != null && !preferredCuisines.isEmpty()) {
            spec = spec.and(preferredCuisinesFilter(preferredCuisines));
        }
        
        spec = andIf(criteria.getMinPrepTime() != null, spec,
                greaterThanOrEqualSpec("prepTimeMinutes", criteria.getMinPrepTime()));
        spec = andIf(criteria.getMaxPrepTime() != null, spec,
                lessThanOrEqualSpec("prepTimeMinutes", criteria.getMaxPrepTime()));
        
        spec = andIf(criteria.getMinCookTime() != null, spec,
                greaterThanOrEqualSpec("cookTimeMinutes", criteria.getMinCookTime()));
        spec = andIf(criteria.getMaxCookTime() != null, spec,
                lessThanOrEqualSpec("cookTimeMinutes", criteria.getMaxCookTime()));
        
        spec = andIf(criteria.getMinTotalTime() != null, spec,
                greaterThanOrEqualSpec("totalTimeMinutes", criteria.getMinTotalTime()));
        spec = andIf(criteria.getMaxTotalTime() != null, spec,
                lessThanOrEqualSpec("totalTimeMinutes", criteria.getMaxTotalTime()));
        
        spec = andIf(criteria.getMinServings() != null, spec,
                greaterThanOrEqualSpec("servings", criteria.getMinServings()));
        spec = andIf(criteria.getMaxServings() != null, spec,
                lessThanOrEqualSpec("servings", criteria.getMaxServings()));
        
        if (criteria.getMinCalories() != null || criteria.getMaxCalories() != null) {
            spec = spec.and(caloriesFilter(criteria.getMinCalories(), criteria.getMaxCalories()));
        }
        
        spec = andIf(criteria.getDietaryRestrictions() != null && !criteria.getDietaryRestrictions().isEmpty(),
                spec,
                DietaryRestrictionFilter.filter(criteria.getDietaryRestrictions(), criteria.getRestrictionMatchMode()));
        
        boolean excludeAllergens = Boolean.TRUE.equals(criteria.getExcludeAllergens());
        spec = andIf(excludeAllergens && userAllergies != null && !userAllergies.isEmpty(),
                spec,
                allergenFilter(userAllergies));
        
        // Only show public recipes in search results
        spec = spec.and(equalsSpec("isPublic", true));
        
        return spec;
    }

    private static Specification<Recipe> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    private static Specification<Recipe> equalsSpec(String field, Object value) {
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private static <T extends Comparable<? super T>> Specification<Recipe> greaterThanOrEqualSpec(String field, T value) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(field), value);
    }

    private static <T extends Comparable<? super T>> Specification<Recipe> lessThanOrEqualSpec(String field, T value) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(field), value);
    }

    private static Specification<Recipe> andIf(boolean condition, Specification<Recipe> spec,
                                               Specification<Recipe> addition) {
        return condition ? spec.and(addition) : spec;
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
        return (root, query, cb) -> root.get("cuisineType").in(preferredCuisines);
    }
}
