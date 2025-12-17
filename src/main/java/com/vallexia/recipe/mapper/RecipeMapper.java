package com.vallexia.recipe.mapper;

import com.vallexia.recipe.dto.*;
import com.vallexia.recipe.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
    @Mapping(target = "nutritionalInfo", ignore = true)
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
    @Mapping(target = "nutritionalInfo", ignore = true)
    RecipeDto toRecipeDto(Recipe recipe);
    
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
