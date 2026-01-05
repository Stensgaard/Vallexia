package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing an ingredient from Spoonacular API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularIngredientDto {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("nameClean")
    private String nameClean;
    
    @JsonProperty("original")
    private String original;
    
    @JsonProperty("originalName")
    private String originalName;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("measures")
    private SpoonacularMeasuresDto measures;
    
    @JsonProperty("image")
    private String image;
}
