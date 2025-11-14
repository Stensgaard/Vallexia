package com.vallexia.recipe.controller;

import com.vallexia.recipe.dto.*;
import com.vallexia.recipe.service.*;
import com.vallexia.security.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for recipe management endpoints.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/recipes")
@Tag(name = "Recipe Management", description = "Operations related to recipe management, search, and favorites")
public class RecipeController {
    
    private final RecipeService recipeService;
    private final RecipeSearchService recipeSearchService;
    private final RecipeScalingService recipeScalingService;
    private final FavoriteRecipeService favoriteRecipeService;
    private final AuthenticationHelper authenticationHelper;
    
    /**
     * Constructor for dependency injection.
     */
    public RecipeController(
            RecipeService recipeService,
            RecipeSearchService recipeSearchService,
            RecipeScalingService recipeScalingService,
            FavoriteRecipeService favoriteRecipeService,
            AuthenticationHelper authenticationHelper) {
        this.recipeService = recipeService;
        this.recipeSearchService = recipeSearchService;
        this.recipeScalingService = recipeScalingService;
        this.favoriteRecipeService = favoriteRecipeService;
        this.authenticationHelper = authenticationHelper;
    }
    
    /**
     * List all public recipes with pagination.
     */
    @Operation(summary = "List public recipes", description = "Get a paginated list of all public recipes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recipes retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<Page<RecipeDto>> getAllRecipes(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("Getting all public recipes - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RecipeDto> recipes = recipeService.getPublicRecipes(pageable);
        
        return ResponseEntity.ok(recipes);
    }
    
    /**
     * Get recipe by ID.
     */
    @Operation(summary = "Get recipe by ID", description = "Retrieve a specific recipe by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recipe retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Recipe not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(
            @Parameter(description = "Recipe ID") @PathVariable Long id,
            Authentication authentication) {
        log.debug("Getting recipe ID {}", id);
        
        Long userId = authentication != null ? authenticationHelper.getCurrentUserId(authentication) : null;
        RecipeDto recipe = recipeService.getRecipeById(id, userId);
        
        return ResponseEntity.ok(recipe);
    }
    
    /**
     * Create a new recipe.
     */
    @Operation(summary = "Create recipe", description = "Create a new recipe (requires admin role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Recipe created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecipeDto> createRecipe(
            @Parameter(description = "Recipe creation data") @Valid @RequestBody CreateRecipeDto dto,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Creating recipe '{}' by user ID {}", dto.getName(), userId);
        
        RecipeDto createdRecipe = recipeService.createRecipe(dto, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
    }
    
    /**
     * Update an existing recipe.
     */
    @Operation(summary = "Update recipe", description = "Update an existing recipe (requires admin role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recipe updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecipeDto> updateRecipe(
            @Parameter(description = "Recipe ID") @PathVariable Long id,
            @Parameter(description = "Recipe update data") @Valid @RequestBody UpdateRecipeDto dto,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Updating recipe ID {} by user ID {}", id, userId);
        
        RecipeDto updatedRecipe = recipeService.updateRecipe(id, dto, userId);
        
        return ResponseEntity.ok(updatedRecipe);
    }
    
    /**
     * Delete a recipe.
     */
    @Operation(summary = "Delete recipe", description = "Delete a recipe (requires admin role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Recipe deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRecipe(
            @Parameter(description = "Recipe ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Deleting recipe ID {} by user ID {}", id, userId);
        
        recipeService.deleteRecipe(id, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Advanced recipe search with multiple filters.
     */
    @Operation(summary = "Search recipes", description = "Search recipes with advanced filtering and sorting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search criteria")
    })
    @GetMapping("/search")
    public ResponseEntity<RecipeSearchResponseDto> searchRecipes(
            @ModelAttribute RecipeSearchCriteria criteria,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.debug("Searching recipes with criteria: {}", criteria);
        
        Pageable pageable = PageRequest.of(page, size);
        RecipeSearchResponseDto response = recipeSearchService.searchRecipes(criteria, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get scaled recipe for a specific number of servings.
     */
    @Operation(summary = "Scale recipe", description = "Get recipe scaled to a different number of servings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Scaled recipe retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Recipe not found"),
        @ApiResponse(responseCode = "400", description = "Invalid servings number")
    })
    @GetMapping("/{id}/scale")
    public ResponseEntity<RecipeDto> scaleRecipe(
            @Parameter(description = "Recipe ID") @PathVariable Long id,
            @Parameter(description = "Target number of servings") @RequestParam Integer servings,
            Authentication authentication) {
        log.debug("Scaling recipe ID {} to {} servings", id, servings);
        
        Long userId = authentication != null ? authenticationHelper.getCurrentUserId(authentication) : null;
        RecipeDto scaledRecipe = recipeScalingService.scaleRecipe(id, servings, userId);
        
        return ResponseEntity.ok(scaledRecipe);
    }
    
    /**
     * Add recipe to favorites.
     */
    @Operation(summary = "Add to favorites", description = "Add a recipe to user's favorites (requires authentication)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recipe added to favorites"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    @PostMapping("/{id}/favorite")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> addFavorite(
            @Parameter(description = "Recipe ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Adding recipe ID {} to favorites for user ID {}", id, userId);
        
        favoriteRecipeService.addFavorite(id, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Remove recipe from favorites.
     */
    @Operation(summary = "Remove from favorites", description = "Remove a recipe from user's favorites (requires authentication)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Recipe removed from favorites"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}/favorite")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeFavorite(
            @Parameter(description = "Recipe ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.info("Removing recipe ID {} from favorites for user ID {}", id, userId);
        
        favoriteRecipeService.removeFavorite(id, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get user's favorite recipes.
     */
    @Operation(summary = "Get favorites", description = "Get all recipes favorited by the current user (requires authentication)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Favorites retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/favorites")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<RecipeDto>> getFavorites(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        log.debug("Getting favorites for user ID {}", userId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RecipeDto> favorites = favoriteRecipeService.getUserFavorites(userId, pageable);
        
        return ResponseEntity.ok(favorites);
    }
}
