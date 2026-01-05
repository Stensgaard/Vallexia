package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO representing a step in analyzed instructions from Spoonacular API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularStepDto {
    
    @JsonProperty("number")
    private Integer number;
    
    @JsonProperty("step")
    private String step;
    
    @JsonProperty("ingredients")
    private List<SpoonacularStepIngredientDto> ingredients;
    
    @JsonProperty("equipment")
    private List<SpoonacularStepEquipmentDto> equipment;
}
