package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing a single measure from Spoonacular API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularMeasureDto {
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("unitShort")
    private String unitShort;
    
    @JsonProperty("unitLong")
    private String unitLong;
}
