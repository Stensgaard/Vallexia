package com.vallexia.recipe.integration.client;

import com.vallexia.config.api.SpoonacularProperties;
import com.vallexia.recipe.integration.dto.*;
import com.vallexia.recipe.integration.exception.SpoonacularApiException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Client for interacting with Spoonacular API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Slf4j
@Component
public class SpoonacularApiClient {
    
    private final RestClient restClient;
    private final SpoonacularProperties properties;
    
    public SpoonacularApiClient(SpoonacularProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    /**
     * Search recipes using complex search endpoint.
     * 
     * @param params search parameters
     * @return search response with recipes
     * @throws SpoonacularApiException if API call fails
     */
    public SpoonacularSearchResponseDto searchRecipes(SpoonacularSearchParams params) {
        log.debug("Searching recipes with params: {}", params);
        
        try {
            var uriSpec = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/recipes/complexSearch")
                                .queryParam("apiKey", properties.getApiKey());
                        
                        if (params.getQuery() != null && !params.getQuery().isBlank()) {
                            uriBuilder.queryParam("query", params.getQuery());
                        }
                        if (params.getIncludeIngredients() != null && !params.getIncludeIngredients().isEmpty()) {
                            uriBuilder.queryParam("includeIngredients", 
                                    String.join(",", params.getIncludeIngredients()));
                        }
                        if (params.getExcludeIngredients() != null && !params.getExcludeIngredients().isEmpty()) {
                            uriBuilder.queryParam("excludeIngredients", 
                                    String.join(",", params.getExcludeIngredients()));
                        }
                        if (params.getDiet() != null && !params.getDiet().isBlank()) {
                            uriBuilder.queryParam("diet", params.getDiet());
                        }
                        if (params.getIntolerances() != null && !params.getIntolerances().isEmpty()) {
                            uriBuilder.queryParam("intolerances", 
                                    String.join(",", params.getIntolerances()));
                        }
                        if (params.getCuisine() != null && !params.getCuisine().isEmpty()) {
                            uriBuilder.queryParam("cuisine", String.join(",", params.getCuisine()));
                        }
                        if (params.getExcludeCuisine() != null && !params.getExcludeCuisine().isEmpty()) {
                            uriBuilder.queryParam("excludeCuisine", 
                                    String.join(",", params.getExcludeCuisine()));
                        }
                        if (params.getNumber() != null) {
                            uriBuilder.queryParam("number", params.getNumber());
                        }
                        if (params.getOffset() != null) {
                            uriBuilder.queryParam("offset", params.getOffset());
                        }
                        if (params.getAddRecipeInformation() != null && params.getAddRecipeInformation()) {
                            uriBuilder.queryParam("addRecipeInformation", "true");
                        }
                        if (params.getAddRecipeInstructions() != null && params.getAddRecipeInstructions()) {
                            uriBuilder.queryParam("addRecipeInstructions", "true");
                        }
                        if (params.getAddRecipeNutrition() != null && params.getAddRecipeNutrition()) {
                            uriBuilder.queryParam("addRecipeNutrition", "true");
                        }
                        if (params.getFillIngredients() != null && params.getFillIngredients()) {
                            uriBuilder.queryParam("fillIngredients", "true");
                        }
                        
                        return uriBuilder.build();
                    });
            
            SpoonacularSearchResponseDto response = uriSpec.retrieve()
                    .onStatus(status -> status.isError(), (request, response1) -> {
                        log.error("Spoonacular API error: {} {}", response1.getStatusCode(), 
                                response1.getStatusText());
                        throw new SpoonacularApiException(
                                "Spoonacular API error: " + response1.getStatusCode());
                    })
                    .body(SpoonacularSearchResponseDto.class);
            
            log.debug("Found {} recipes", response.getTotalResults());
            return response;
            
        } catch (RestClientException e) {
            log.error("Error calling Spoonacular API", e);
            throw new SpoonacularApiException("Failed to search recipes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get recipe by ID with full information.
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @return recipe DTO
     * @throws SpoonacularApiException if API call fails
     */
    public SpoonacularRecipeDto getRecipeById(Integer spoonacularId) {
        log.debug("Getting recipe by ID: {}", spoonacularId);
        
        try {
            SpoonacularRecipeDto recipe = restClient.get()
                    .uri("/recipes/{id}/information?apiKey={apiKey}&includeNutrition=true", 
                            spoonacularId, properties.getApiKey())
                    .retrieve()
                    .onStatus(status -> status.isError(), (request, response) -> {
                        log.error("Spoonacular API error: {} {}", response.getStatusCode(), 
                                response.getStatusText());
                        throw new SpoonacularApiException(
                                "Spoonacular API error: " + response.getStatusCode());
                    })
                    .body(SpoonacularRecipeDto.class);
            
            log.debug("Retrieved recipe: {} with {} ingredients", 
                    recipe.getTitle(), 
                    recipe.getExtendedIngredients() != null ? recipe.getExtendedIngredients().size() : 0);
            return recipe;
            
        } catch (RestClientException e) {
            log.error("Error calling Spoonacular API for recipe ID: {}", spoonacularId, e);
            throw new SpoonacularApiException(
                    "Failed to get recipe: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get multiple recipes by IDs in bulk.
     * 
     * @param ids list of Spoonacular recipe IDs
     * @return list of recipe DTOs
     * @throws SpoonacularApiException if API call fails
     */
    public List<SpoonacularRecipeDto> getRecipesBulk(List<Integer> ids) {
        log.debug("Getting {} recipes in bulk", ids.size());
        
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        
        try {
            String idsParam = ids.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            
            List<SpoonacularRecipeDto> recipes = restClient.get()
                    .uri("/recipes/informationBulk?apiKey={apiKey}&ids={ids}&includeNutrition=true", 
                            properties.getApiKey(), idsParam)
                    .retrieve()
                    .onStatus(status -> status.isError(), (request, response) -> {
                        log.error("Spoonacular API error: {} {}", response.getStatusCode(), 
                                response.getStatusText());
                        throw new SpoonacularApiException(
                                "Spoonacular API error: " + response.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<List<SpoonacularRecipeDto>>() {});
            
            log.debug("Retrieved {} recipes", recipes.size());
            return recipes;
            
        } catch (RestClientException e) {
            log.error("Error calling Spoonacular API for bulk recipes", e);
            throw new SpoonacularApiException(
                    "Failed to get recipes in bulk: " + e.getMessage(), e);
        }
    }
}
