package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing caloric breakdown from Spoonacular API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularCaloricBreakdownDto {
    
    @JsonProperty("percentProtein")
    private Double percentProtein;
    
    @JsonProperty("percentFat")
    private Double percentFat;
    
    @JsonProperty("percentCarbs")
    private Double percentCarbs;
}
