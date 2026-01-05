package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO representing a recipe from Spoonacular API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularRecipeDto {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("instructions")
    private String instructions;
    
    @JsonProperty("preparationMinutes")
    private Integer preparationMinutes;
    
    @JsonProperty("cookingMinutes")
    private Integer cookingMinutes;
    
    @JsonProperty("readyInMinutes")
    private Integer readyInMinutes;
    
    @JsonProperty("servings")
    private Integer servings;
    
    @JsonProperty("image")
    private String image;
    
    @JsonProperty("imageType")
    private String imageType;
    
    @JsonProperty("cuisines")
    private List<String> cuisines;
    
    @JsonProperty("dishTypes")
    private List<String> dishTypes;
    
    @JsonProperty("diets")
    private List<String> diets;
    
    @JsonProperty("extendedIngredients")
    private List<SpoonacularIngredientDto> extendedIngredients;
    
    @JsonProperty("analyzedInstructions")
    private List<SpoonacularAnalyzedInstructionDto> analyzedInstructions;
    
    @JsonProperty("nutrition")
    private SpoonacularNutritionDto nutrition;
    
    @JsonProperty("spoonacularSourceUrl")
    private String spoonacularSourceUrl;
    
    @JsonProperty("sourceUrl")
    private String sourceUrl;
}
