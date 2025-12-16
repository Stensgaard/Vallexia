package com.vallexia.recipe.service.specification;

import com.vallexia.recipe.dto.RecipeSearchCriteria;
import com.vallexia.recipe.entity.enums.RecipeSortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// TODO remove this class

/**
 * Helper class for applying sorting to recipe search queries.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
public final class RecipeSortHelper {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private RecipeSortHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Apply sorting based on criteria.
     * 
     * @param pageable pagination information
     * @param criteria search criteria containing sort information
     * @return pageable with sorting applied
     */
    public static Pageable applySorting(Pageable pageable, RecipeSearchCriteria criteria) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id"); // Default sort
        
        if (criteria.getSortBy() != null) {
            Sort.Direction direction = criteria.getSortOrder() == RecipeSortOrder.ASC 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC;
            
            switch (criteria.getSortBy()) {
                case NAME:
                    sort = Sort.by(direction, "name");
                    break;
                case CREATED_DATE:
                    sort = Sort.by(direction, "createdAt");
                    break;
                case PREP_TIME:
                    sort = Sort.by(direction, "prepTimeMinutes");
                    break;
                case COOK_TIME:
                    sort = Sort.by(direction, "cookTimeMinutes");
                    break;
                case TOTAL_TIME:
                    sort = Sort.by(direction, "totalTimeMinutes");
                    break;
                case SERVINGS:
                    sort = Sort.by(direction, "servings");
                    break;
                case CALORIES:
                    // For calories, we need a custom sort that joins nutritional info
                    sort = Sort.by(direction, "nutritionalInfo.calories");
                    break;
                default:
                    sort = Sort.by(direction, "createdAt");
            }
        }
        
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
