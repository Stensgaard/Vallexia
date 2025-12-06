package com.vallexia.recipe.mapper;

import com.vallexia.recipe.dto.*;
import com.vallexia.recipe.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for converting between Recipe entities and DTOs.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RecipeMapper {
    
    /**
     * Convert Recipe entity to RecipeDto with favorite flag.
     * 
     * @param recipe Recipe entity
     * @param isFavorite whether the recipe is favorited by the current user
     * @return RecipeDto
     */
    @Mapping(target = "creatorId", source = "recipe.creator.id")
    @Mapping(target = "creatorUsername", source = "recipe.creator.username")
    @Mapping(target = "isFavorite", source = "isFavorite")
    @Mapping(target = "ingredients", source = "recipe.ingredients")
    @Mapping(target = "nutritionalInfo", source = "recipe.nutritionalInfo")
    RecipeDto toRecipeDto(Recipe recipe, Boolean isFavorite);
    
    /**
     * Convert Recipe entity to RecipeDto without favorite flag (defaults to false).
     * 
     * @param recipe Recipe entity
     * @return RecipeDto
     */
    @Mapping(target = "creatorId", source = "creator.id")
    @Mapping(target = "creatorUsername", source = "creator.username")
    @Mapping(target = "isFavorite", constant = "false")
    @Mapping(target = "ingredients", source = "ingredients")
    @Mapping(target = "nutritionalInfo", source = "nutritionalInfo")
    RecipeDto toRecipeDto(Recipe recipe);
    
    /**
     * Convert CreateRecipeDto to Recipe entity.
     * 
     * @param dto CreateRecipeDto
     * @return Recipe entity (without ID, timestamps, and relationships)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "nutritionalInfo", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "favoriteRecipes", ignore = true)
    @Mapping(target = "totalTimeMinutes", ignore = true)
    @Mapping(target = "baseLocale", ignore = true) // Base locale is set from admin's settings, not from DTO
    @Mapping(target = "translations", ignore = true) // Translations are handled separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // dietaryRestrictions and allergens are automatically mapped by MapStruct
    Recipe toRecipe(CreateRecipeDto dto);
    
    /**
     * Update Recipe entity from UpdateRecipeDto (partial update).
     * 
     * @param dto UpdateRecipeDto with updated fields
     * @param recipe Recipe entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "nutritionalInfo", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "favoriteRecipes", ignore = true)
    @Mapping(target = "baseLocale", ignore = true) // Base locale is preserved from original recipe, not updated
    @Mapping(target = "translations", ignore = true) // Translations are handled separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // dietaryRestrictions and allergens are automatically mapped by MapStruct
    void updateRecipeFromDto(UpdateRecipeDto dto, @MappingTarget Recipe recipe);
    
    /**
     * Convert RecipeIngredient entity to IngredientDto.
     * 
     * @param recipeIngredient RecipeIngredient entity
     * @return IngredientDto
     */
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "name", source = "ingredient.name")
    IngredientDto toIngredientDto(RecipeIngredient recipeIngredient);
    
    /**
     * Convert list of RecipeIngredient entities to list of IngredientDto.
     * 
     * @param recipeIngredients List of RecipeIngredient entities
     * @return List of IngredientDto
     */
    List<IngredientDto> toIngredientDtoList(List<RecipeIngredient> recipeIngredients);
    
    /**
     * Convert NutritionalInfo entity to NutritionalInfoDto.
     * 
     * @param nutritionalInfo NutritionalInfo entity
     * @return NutritionalInfoDto
     */
    NutritionalInfoDto toNutritionalInfoDto(NutritionalInfo nutritionalInfo);
    
    /**
     * Convert NutritionalInfoDto to NutritionalInfo entity.
     * 
     * @param dto NutritionalInfoDto
     * @return NutritionalInfo entity (without ID, recipe, and timestamps)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recipe", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NutritionalInfo toNutritionalInfo(NutritionalInfoDto dto);
    
    /**
     * Convert IngredientDto to RecipeIngredient entity.
     * Note: This creates a RecipeIngredient but doesn't set the recipe or ingredient relationships.
     * These should be set manually in the service layer.
     * 
     * @param dto IngredientDto
     * @return RecipeIngredient entity (without recipe and ingredient relationships)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recipe", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    RecipeIngredient toRecipeIngredient(IngredientDto dto);
}
