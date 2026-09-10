package com.vallexia.recipe.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO representing search results from Spoonacular API.
 * 
 * @since 2025-12-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpoonacularSearchResponseDto {
    
    @JsonProperty("results")
    private List<SpoonacularRecipeDto> results;
    
    @JsonProperty("offset")
    private Integer offset;
    
    @JsonProperty("number")
    private Integer number;
    
    @JsonProperty("totalResults")
    private Integer totalResults;
}
