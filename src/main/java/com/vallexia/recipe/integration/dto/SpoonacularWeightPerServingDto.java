package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing weight per serving from Spoonacular API.
 * 
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularWeightPerServingDto {
    
    @JsonProperty("amount")
    private Integer amount;
    
    @JsonProperty("unit")
    private String unit;
}
