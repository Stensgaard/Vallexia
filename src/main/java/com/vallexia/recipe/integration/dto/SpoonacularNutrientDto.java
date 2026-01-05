package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing a nutrient from Spoonacular API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularNutrientDto {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("percentOfDailyNeeds")
    private Double percentOfDailyNeeds;
}
