package com.vallexia.recipe.service;

import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.dto.RecipeSearchCriteria;
import com.vallexia.recipe.dto.RecipeSearchResponseDto;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.RecipeRepository;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for advanced recipe search with multiple filters and criteria.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RecipeSearchService {
    
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param recipeRepository the recipe repository
     * @param recipeMapper the recipe mapper
     */
    public RecipeSearchService(RecipeRepository recipeRepository, RecipeMapper recipeMapper) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
    }
    
    /**
     * Search recipes with advanced filtering criteria.
     * 
     * @param criteria search criteria
     * @param pageable pagination information
     * @return search response with paginated results
     */
    public RecipeSearchResponseDto searchRecipes(RecipeSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching recipes with criteria: {}", criteria);
        
        // Build specification from criteria
        Specification<Recipe> spec = buildSpecification(criteria);
        
        // Apply sorting
        Pageable sortedPageable = applySorting(pageable, criteria);
        
        // Execute search
        Page<Recipe> recipePage = recipeRepository.findAll(spec, sortedPageable);
        
        // Convert to DTOs
        Page<RecipeDto> recipeDtoPage = recipePage.map(recipe -> recipeMapper.toRecipeDto(recipe, false));
        
        // Build response
        RecipeSearchResponseDto response = new RecipeSearchResponseDto();
        response.setRecipes(recipeDtoPage.getContent());
        
        RecipeSearchResponseDto.PaginationInfo pagination = new RecipeSearchResponseDto.PaginationInfo();
        pagination.setPage(recipeDtoPage.getNumber());
        pagination.setSize(recipeDtoPage.getSize());
        pagination.setTotalElements(recipeDtoPage.getTotalElements());
        pagination.setTotalPages(recipeDtoPage.getTotalPages());
        pagination.setHasNext(recipeDtoPage.hasNext());
        pagination.setHasPrevious(recipeDtoPage.hasPrevious());
        response.setPagination(pagination);
        
        log.debug("Found {} recipes matching criteria", recipeDtoPage.getTotalElements());
        return response;
    }
    
    /**
     * Build JPA Specification from search criteria.
     * 
     * @param criteria search criteria
     * @return JPA Specification
     */
    private Specification<Recipe> buildSpecification(RecipeSearchCriteria criteria) {
        Specification<Recipe> spec = (root, query, cb) -> cb.conjunction();
        
        // Text search on name and description
        if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
            spec = spec.and(textSearch(criteria.getQuery().trim()));
        }
        
        // Category filter
        if (criteria.getCategory() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), criteria.getCategory()));
        }
        
        // Cuisine type filter
        if (criteria.getCuisineType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cuisineType"), criteria.getCuisineType()));
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
        
        // Ingredient search
        if (criteria.getIngredients() != null && !criteria.getIngredients().isEmpty()) {
            spec = spec.and(ingredientSearch(criteria.getIngredients()));
        }
        
        // Dietary restrictions filter
        if (criteria.getDietaryRestrictions() != null && !criteria.getDietaryRestrictions().isEmpty()) {
            // This is complex - would need to check ingredients against restrictions
            // For now, we'll implement basic filtering
            // Future: Check ingredient names against restriction requirements
            // TODO: implement this
        }
        
        // Only show public recipes (unless user is viewing their own)
        // TODO: users can no longer make their own recipes
        spec = spec.and((root, query, cb) -> cb.equal(root.get("isPublic"), true));
        
        return spec;
    }
    
    /**
     * Text search specification for name and description.
     */
    private Specification<Recipe> textSearch(String query) {
        return (root, queryBuilder, cb) -> {
            String searchPattern = "%" + query.toLowerCase() + "%";
            Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchPattern);
            Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
            return cb.or(nameMatch, descMatch);
        };
    }
    
    /**
     * Calories filter specification.
     */
    private Specification<Recipe> caloriesFilter(BigDecimal minCalories, BigDecimal maxCalories) {
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
     * Ingredient search specification.
     */
    private Specification<Recipe> ingredientSearch(List<String> ingredientNames) {
        return (root, queryBuilder, cb) -> {
            Join<Recipe, com.vallexia.recipe.entity.RecipeIngredient> recipeIngredientJoin = 
                    root.join("ingredients", JoinType.INNER);
            Join<com.vallexia.recipe.entity.RecipeIngredient, com.vallexia.recipe.entity.Ingredient> ingredientJoin = 
                    recipeIngredientJoin.join("ingredient", JoinType.INNER);
            
            List<Predicate> predicates = new ArrayList<>();
            for (String ingredientName : ingredientNames) {
                String searchPattern = "%" + ingredientName.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(ingredientJoin.get("name")), searchPattern));
            }
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Apply sorting based on criteria.
     */
    private Pageable applySorting(Pageable pageable, RecipeSearchCriteria criteria) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id"); // Default sort
        
        if (criteria.getSortBy() != null) {
            Sort.Direction direction = criteria.getSortOrder() == RecipeSearchCriteria.SortOrder.ASC 
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
