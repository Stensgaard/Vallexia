package com.vallexia.recipe.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.recipe.dto.CreateRecipeDto;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.dto.UpdateRecipeDto;
import com.vallexia.recipe.entity.*;
import com.vallexia.exception.ValidationException;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.*;
import com.vallexia.security.AuthenticationHelper;
import com.vallexia.user.entity.User;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing recipe CRUD operations and business logic.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final NutritionalInfoRepository nutritionalInfoRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;
    private final FavoriteRecipeService favoriteRecipeService;
    private final NutritionalCalculationService nutritionalCalculationService;
    private final AuditService auditService;
    private final AuthenticationHelper authenticationHelper;
    
    /**
     * Constructor for dependency injection.
     */
    public RecipeService(
            RecipeRepository recipeRepository,
            IngredientRepository ingredientRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            NutritionalInfoRepository nutritionalInfoRepository,
            UserRepository userRepository,
            RecipeMapper recipeMapper,
            FavoriteRecipeService favoriteRecipeService,
            NutritionalCalculationService nutritionalCalculationService,
            AuditService auditService,
            AuthenticationHelper authenticationHelper) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.nutritionalInfoRepository = nutritionalInfoRepository;
        this.userRepository = userRepository;
        this.recipeMapper = recipeMapper;
        this.favoriteRecipeService = favoriteRecipeService;
        this.nutritionalCalculationService = nutritionalCalculationService;
        this.auditService = auditService;
        this.authenticationHelper = authenticationHelper;
    }
    
    /**
     * Create a new recipe.
     * 
     * @param dto recipe creation DTO
     * @param userId creator user ID
     * @return created recipe DTO
     */
    public RecipeDto createRecipe(CreateRecipeDto dto, Long userId) {
        log.info("Creating recipe '{}' for user ID {}", dto.getName(), userId);
        
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        // Convert DTO to entity
        Recipe recipe = recipeMapper.toRecipe(dto);
        recipe.setCreator(creator);
        
        // Calculate total time if prep and cook times are provided
        if (dto.getPrepTimeMinutes() != null && dto.getCookTimeMinutes() != null) {
            recipe.calculateTotalTime();
        }
        
        // Save recipe first to get ID
        recipe = recipeRepository.save(recipe);
        
        // Handle ingredients
        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            List<RecipeIngredient> recipeIngredients = processIngredients(dto.getIngredients(), recipe);
            recipe.setIngredients(recipeIngredients);
        }
        
        // Handle tags (validation ensures at least 1 via @NotEmpty)
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (String tag : dto.getTags()) {
                recipe.addTag(tag);
            }
        }
        
        // Handle nutritional info
        if (dto.getNutritionalInfo() != null) {
            NutritionalInfo nutritionalInfo = recipeMapper.toNutritionalInfo(dto.getNutritionalInfo());
            nutritionalInfo.setRecipe(recipe);
            nutritionalInfoRepository.save(nutritionalInfo);
        } else {
            // Calculate nutrition from ingredients if available
            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                nutritionalCalculationService.updateRecipeNutrition(recipe);
            }
        }
        
        // Save final recipe
        recipe = recipeRepository.save(recipe);
        
        // Audit log
        auditService.logEvent(
            EventType.RECIPE_CREATED,
            userId,
            String.format("Recipe '%s' (ID: %d) created by user ID %d", recipe.getName(), recipe.getId(), userId)
        );
        
        log.info("Recipe created successfully with ID {}", recipe.getId());
        
        // Check if favorited
        boolean isFavorite = favoriteRecipeService.isFavorite(recipe.getId(), userId);
        return recipeMapper.toRecipeDto(recipe, isFavorite);
    }
    
    /**
     * Get recipe by ID.
     * 
     * @param id recipe ID
     * @param userId current user ID (for favorite check)
     * @return recipe DTO
     * @throws RecipeNotFoundException if recipe not found
     */
    @Transactional(readOnly = true)
    public RecipeDto getRecipeById(Long id, Long userId) {
        log.debug("Getting recipe ID {} for user ID {}", id, userId);
        
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));
        
        // Check if user can access (must be public or creator)
        if (!recipe.getIsPublic() && (userId == null || !recipe.getCreator().getId().equals(userId))) {
            throw new AccessDeniedException("You do not have permission to access this recipe");
        }
        
        boolean isFavorite = userId != null && favoriteRecipeService.isFavorite(id, userId);
        return recipeMapper.toRecipeDto(recipe, isFavorite);
    }
    
    /**
     * Update an existing recipe.
     * 
     * @param id recipe ID
     * @param dto update DTO
     * @param userId user ID (must be admin)
     * @return updated recipe DTO
     */
    public RecipeDto updateRecipe(Long id, UpdateRecipeDto dto, Long userId) {
        log.info("Updating recipe ID {} by user ID {}", id, userId);
        
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));
        
        // Only admins can update recipes (enforced at controller level)
        boolean isAdmin = authenticationHelper.hasRole("ROLE_ADMIN");
        if (!isAdmin) {
            throw new AccessDeniedException("You do not have permission to update this recipe");
        }
        
        // Update basic fields
        recipeMapper.updateRecipeFromDto(dto, recipe);
        
        // Recalculate total time if needed
        if (dto.getPrepTimeMinutes() != null || dto.getCookTimeMinutes() != null) {
            recipe.calculateTotalTime();
        }
        
        // Update ingredients if provided
        if (dto.getIngredients() != null) {
            // Delete existing ingredients
            recipeIngredientRepository.deleteByRecipeId(id);
            
            // Add new ingredients
            List<RecipeIngredient> recipeIngredients = processIngredients(dto.getIngredients(), recipe);
            recipe.setIngredients(recipeIngredients);
        }
        
        // Update tags if provided
        if (dto.getTags() != null) {
            if (dto.getTags().isEmpty()) {
                throw new ValidationException("At least one tag is required");
            }
            recipe.getTags().clear();
            for (String tag : dto.getTags()) {
                recipe.addTag(tag);
            }
        }
        
        // Update dietary restrictions if provided
        if (dto.getDietaryRestrictions() != null) {
            if (dto.getDietaryRestrictions().isEmpty()) {
                throw new ValidationException("At least one dietary restriction is required");
            }
            // MapStruct automatically updates dietaryRestrictions from DTO to entity
        }
        
        // Update nutritional info if provided
        if (dto.getNutritionalInfo() != null) {
            NutritionalInfo existingInfo = recipe.getNutritionalInfo();
            if (existingInfo != null) {
                // Update existing
                NutritionalInfo updatedInfo = recipeMapper.toNutritionalInfo(dto.getNutritionalInfo());
                existingInfo.setCalories(updatedInfo.getCalories());
                existingInfo.setProtein(updatedInfo.getProtein());
                existingInfo.setCarbs(updatedInfo.getCarbs());
                existingInfo.setFats(updatedInfo.getFats());
                existingInfo.setFiber(updatedInfo.getFiber());
                existingInfo.setSodium(updatedInfo.getSodium());
                existingInfo.setSugar(updatedInfo.getSugar());
                existingInfo.setPerServing(updatedInfo.getPerServing());
                nutritionalInfoRepository.save(existingInfo);
            } else {
                // Create new
                NutritionalInfo nutritionalInfo = recipeMapper.toNutritionalInfo(dto.getNutritionalInfo());
                nutritionalInfo.setRecipe(recipe);
                nutritionalInfoRepository.save(nutritionalInfo);
            }
        } else if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            // Recalculate nutrition if ingredients changed
            nutritionalCalculationService.updateRecipeNutrition(recipe);
        }
        
        recipe = recipeRepository.save(recipe);
        
        // Audit log
        auditService.logEvent(
            EventType.RECIPE_UPDATED,
            userId,
            String.format("Recipe ID %d updated by user ID %d", id, userId)
        );
        
        log.info("Recipe ID {} updated successfully", id);
        
        boolean isFavorite = favoriteRecipeService.isFavorite(id, userId);
        return recipeMapper.toRecipeDto(recipe, isFavorite);
    }
    
    /**
     * Delete a recipe.
     * 
     * @param id recipe ID
     * @param userId user ID (must be admin)
     */
    public void deleteRecipe(Long id, Long userId) {
        log.info("Deleting recipe ID {} by user ID {}", id, userId);
        
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));
        
        // Only admins can delete recipes (enforced at controller level)
        boolean isAdmin = authenticationHelper.hasRole("ROLE_ADMIN");
        if (!isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this recipe");
        }
        
        // Delete recipe (cascade will handle related entities)
        recipeRepository.delete(recipe);
        
        // Audit log
        auditService.logEvent(
            EventType.RECIPE_DELETED,
            userId,
            String.format("Recipe ID %d deleted by user ID %d", id, userId)
        );
        
        log.info("Recipe ID {} deleted successfully", id);
    }
    
    /**
     * Get public recipes.
     * 
     * @param pageable pagination information
     * @param userId current user ID (for favorite check, can be null)
     * @return page of public recipes
     */
    @Transactional(readOnly = true)
    public Page<RecipeDto> getPublicRecipes(Pageable pageable, Long userId) {
        log.debug("Getting public recipes for user ID {}", userId);
        
        Page<Recipe> recipes = recipeRepository.findByIsPublicTrue(pageable);
        return recipes.map(recipe -> {
            boolean isFavorite = userId != null && favoriteRecipeService.isFavorite(recipe.getId(), userId);
            return recipeMapper.toRecipeDto(recipe, isFavorite);
        });
    }
    
    /**
     * Get all recipes (including private) for admin users.
     * 
     * @param pageable pagination information
     * @param userId current user ID (for favorite check)
     * @return page of all recipes
     */
    @Transactional(readOnly = true)
    public Page<RecipeDto> getAllRecipesForAdmin(Pageable pageable, Long userId) {
        log.debug("Getting all recipes for admin user ID {}", userId);
        
        Page<Recipe> recipes = recipeRepository.findAll(pageable);
        return recipes.map(recipe -> {
            boolean isFavorite = userId != null && favoriteRecipeService.isFavorite(recipe.getId(), userId);
            return recipeMapper.toRecipeDto(recipe, isFavorite);
        });
    }

    /**
     * Process ingredients from DTO - find or create ingredients and create recipe ingredients.
     */
    private List<RecipeIngredient> processIngredients(List<com.vallexia.recipe.dto.IngredientDto> ingredientDtos, Recipe recipe) {
        List<RecipeIngredient> recipeIngredients = new ArrayList<>();
        
        for (com.vallexia.recipe.dto.IngredientDto dto : ingredientDtos) {
            // Find or create ingredient
            Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(dto.getName())
                    .orElseGet(() -> {
                        Ingredient newIngredient = new Ingredient();
                        newIngredient.setName(dto.getName());
                        return ingredientRepository.save(newIngredient);
                    });
            
            // Create recipe ingredient
            RecipeIngredient recipeIngredient = recipeMapper.toRecipeIngredient(dto);
            recipeIngredient.setRecipe(recipe);
            recipeIngredient.setIngredient(ingredient);
            
            recipeIngredients.add(recipeIngredient);
        }
        
        return recipeIngredientRepository.saveAll(recipeIngredients);
    }
}
