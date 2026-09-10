package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO representing analyzed instructions from Spoonacular API.
 * 
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularAnalyzedInstructionDto {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("steps")
    private List<SpoonacularStepDto> steps;
}
