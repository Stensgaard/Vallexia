package com.vallexia.recipe.service.specification;

import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.enums.RestrictionMatchMode;
import com.vallexia.recipe.util.AllergenCompatibilityUtil;
import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.DietaryRestriction;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter for dietary restrictions with OR and AND match modes.
 * 
 * Filters recipes based on dietary restrictions with OR or AND logic.
 * For each restriction, a recipe matches if it has the restriction tag OR doesn't have incompatible allergens.
 * For example, DAIRY_FREE matches recipes with DAIRY_FREE tag OR recipes without MILK allergen.
 * 
 * Performance optimization: Pre-computes incompatible allergens once and uses different strategies
 * for OR vs AND mode. OR mode uses LEFT JOINs for better performance, while AND mode uses
 * subqueries which are required for correctness.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class DietaryRestrictionFilter {
    
    /**
     * Create a specification for filtering recipes by dietary restrictions.
     * 
     * @param restrictions list of dietary restrictions to filter by
     * @param matchMode OR (any match) or AND (all must match)
     * @return Specification for dietary restrictions filter
     */
    public static Specification<Recipe> filter(
            List<DietaryRestriction> restrictions, RestrictionMatchMode matchMode) {
        // Pre-compute all incompatible allergens once (performance optimization)
        List<Allergy> allIncompatibleAllergens = AllergenCompatibilityUtil.getIncompatibleAllergens(restrictions);
        
        return (root, query, cb) -> {
            // Note: query is always provided by JPA Specifications framework (linter warnings are false positives)
            if (query != null) {
                query.distinct(true); // Required to avoid duplicates when joining collections
            }
            
            if (matchMode == RestrictionMatchMode.OR) {
                // OR mode: Optimized using LEFT JOINs instead of EXISTS subqueries
                // This is more efficient as it avoids multiple subquery executions
                return filterOR(root, query, cb, restrictions, allIncompatibleAllergens);
            } else {
                // AND mode: Uses subqueries which are required for correctness
                // Each restriction must be satisfied independently
                return filterAND(root, query, cb, restrictions, allIncompatibleAllergens);
            }
        };
    }
    
    /**
     * OR mode dietary restrictions filter (optimized).
     * Recipe matches if it satisfies ANY of the requested restrictions.
     * 
     * Performance optimization: Uses a single join for restriction tags check and a single
     * subquery for incompatible allergens check, rather than per-restriction subqueries.
     * This reduces the number of subqueries from 2N to 2 (where N = number of restrictions).
     * 
     * @param root root entity
     * @param query criteria query
     * @param cb criteria builder
     * @param restrictions list of dietary restrictions
     * @param allIncompatibleAllergens pre-computed incompatible allergens
     * @return predicate for OR mode filtering
     */
    private static Predicate filterOR(
            Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb,
            List<DietaryRestriction> restrictions, List<Allergy> allIncompatibleAllergens) {
        
        List<Predicate> orPredicates = new ArrayList<>();
        
        // Check if recipe has ANY of the requested restriction tags (single join, more efficient)
        Join<Recipe, DietaryRestriction> restrictionsJoin = 
                root.join("dietaryRestrictions", JoinType.LEFT);
        orPredicates.add(restrictionsJoin.in(restrictions));
        
        // Check if recipe doesn't have ANY incompatible allergens (single subquery for all)
        // This is more efficient than checking per-restriction
        if (!allIncompatibleAllergens.isEmpty()) {
            var allergenSubquery = query.subquery(Long.class);
            var allergenSubRoot = allergenSubquery.from(Recipe.class);
            var allergenSubJoin = allergenSubRoot.join("allergens", JoinType.INNER);
            allergenSubquery.select(allergenSubRoot.get("id"))
                    .where(cb.and(
                        cb.equal(allergenSubRoot.get("id"), root.get("id")),
                        allergenSubJoin.in(allIncompatibleAllergens)
                    ));
            // Recipe doesn't have any incompatible allergens
            orPredicates.add(cb.not(cb.exists(allergenSubquery)));
        }
        
        // Recipe matches if it has any restriction tag OR doesn't have incompatible allergens
        return cb.or(orPredicates.toArray(new Predicate[0]));
    }
    
    /**
     * AND mode dietary restrictions filter (uses subqueries for correctness).
     * Recipe matches only if it satisfies ALL requested restrictions.
     * 
     * Performance: Uses subqueries which are necessary for AND logic correctness.
     * Each restriction must be checked independently to ensure all are satisfied.
     * 
     * @param root root entity
     * @param query criteria query
     * @param cb criteria builder
     * @param restrictions list of dietary restrictions
     * @param allIncompatibleAllergens pre-computed incompatible allergens (not used in AND mode, but kept for consistency)
     * @return predicate for AND mode filtering
     */
    private static Predicate filterAND(
            Root<Recipe> root, CriteriaQuery<?> query, CriteriaBuilder cb,
            List<DietaryRestriction> restrictions, List<Allergy> allIncompatibleAllergens) {
        
        List<Predicate> andPredicates = new ArrayList<>();
        
        // For each restriction, create a predicate that matches if:
        // - Recipe has the restriction tag, OR
        // - Recipe doesn't have incompatible allergens for that specific restriction
        for (DietaryRestriction restriction : restrictions) {
            List<Predicate> restrictionOrPredicates = new ArrayList<>();
            
            // Check if recipe has the restriction tag
            var tagSubquery = query.subquery(Long.class);
            var tagSubRoot = tagSubquery.from(Recipe.class);
            var tagSubJoin = tagSubRoot.join("dietaryRestrictions", JoinType.INNER);
            tagSubquery.select(tagSubRoot.get("id"))
                    .where(cb.and(
                        cb.equal(tagSubRoot.get("id"), root.get("id")),
                        cb.equal(tagSubJoin, restriction)
                    ));
            restrictionOrPredicates.add(cb.exists(tagSubquery));
            
            // Check if recipe doesn't have incompatible allergens for this specific restriction
            List<Allergy> restrictionIncompatibleAllergens = AllergenCompatibilityUtil.getIncompatibleAllergens(List.of(restriction));
            if (!restrictionIncompatibleAllergens.isEmpty()) {
                var allergenSubquery = query.subquery(Long.class);
                var allergenSubRoot = allergenSubquery.from(Recipe.class);
                var allergenSubJoin = allergenSubRoot.join("allergens", JoinType.INNER);
                allergenSubquery.select(allergenSubRoot.get("id"))
                        .where(cb.and(
                            cb.equal(allergenSubRoot.get("id"), root.get("id")),
                            allergenSubJoin.in(restrictionIncompatibleAllergens)
                        ));
                // Recipe doesn't have incompatible allergens for this restriction
                restrictionOrPredicates.add(cb.not(cb.exists(allergenSubquery)));
            }
            
            // Recipe matches this restriction if it has the tag OR doesn't have incompatible allergens
            andPredicates.add(cb.or(restrictionOrPredicates.toArray(new Predicate[0])));
        }
        
        // Recipe matches only if it satisfies ALL requested restrictions
        return cb.and(andPredicates.toArray(new Predicate[0]));
    }
}
