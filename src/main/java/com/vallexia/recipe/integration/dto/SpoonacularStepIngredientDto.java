package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing an ingredient in a step from Spoonacular API.
 * 
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularStepIngredientDto {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("localizedName")
    private String localizedName;
    
    @JsonProperty("image")
    private String image;
}
