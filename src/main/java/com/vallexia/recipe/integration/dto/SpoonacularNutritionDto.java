package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO representing nutrition information from Spoonacular API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularNutritionDto {
    
    @JsonProperty("nutrients")
    private List<SpoonacularNutrientDto> nutrients;
    
    @JsonProperty("caloricBreakdown")
    private SpoonacularCaloricBreakdownDto caloricBreakdown;
    
    @JsonProperty("weightPerServing")
    private SpoonacularWeightPerServingDto weightPerServing;
}
