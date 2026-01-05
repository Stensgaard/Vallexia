package com.vallexia.recipe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.recipe.dto.IngredientDto;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.RecipeTranslationCache;
import com.vallexia.recipe.integration.client.GoogleTranslationClient;
import com.vallexia.recipe.integration.exception.GoogleTranslationException;
import com.vallexia.recipe.repository.IngredientRepository;
import com.vallexia.recipe.repository.RecipeTranslationCacheRepository;
import com.vallexia.recipe.util.TranslationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for localizing RecipeDto with translations using Google Cloud Translation API.
 * Handles translation caching and ingredient name translations.
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional
public class RecipeLocalizationService {
    
    private final GoogleTranslationClient translationClient;
    private final RecipeTranslationCacheRepository translationCacheRepository;
    private final TranslationResolver translationResolver;
    private final IngredientRepository ingredientRepository;
    private final ObjectMapper objectMapper;
    
    public RecipeLocalizationService(
            GoogleTranslationClient translationClient,
            RecipeTranslationCacheRepository translationCacheRepository,
            TranslationResolver translationResolver,
            IngredientRepository ingredientRepository,
            ObjectMapper objectMapper) {
        this.translationClient = translationClient;
        this.translationCacheRepository = translationCacheRepository;
        this.translationResolver = translationResolver;
        this.ingredientRepository = ingredientRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Enrich RecipeDto with translations for a given locale.
     * 
     * @param dto the recipe DTO to enrich
     * @param spoonacularId the Spoonacular recipe ID
     * @param userLocale the user's locale
     * @return enriched RecipeDto with translated content
     */
    public RecipeDto enrichWithTranslations(RecipeDto dto, Integer spoonacularId, String userLocale) {
        if (dto == null || spoonacularId == null) {
            return dto;
        }
        
        // Validate and normalize locale
        String locale = validateAndNormalizeLocale(userLocale);
        
        // If locale is English (Spoonacular default), no translation needed
        if (SupportedLocale.EN.getCode().equals(locale)) {
            log.debug("Locale is English, skipping translation for recipe ID {}", spoonacularId);
            enrichIngredientNames(dto, locale);
            return dto;
        }
        
        // Check translation cache
        LocalDateTime now = LocalDateTime.now();
        Optional<RecipeTranslationCache> cachedTranslation = 
                translationCacheRepository.findBySpoonacularIdAndLocaleAndNotExpired(
                        spoonacularId, locale, now);
        
        if (cachedTranslation.isPresent()) {
            log.debug("Using cached translation for recipe ID {} in locale {}", spoonacularId, locale);
            RecipeTranslationCache cache = cachedTranslation.get();
            applyCachedTranslation(dto, cache);
            enrichIngredientNames(dto, locale);
            return dto;
        }
        
        // Translation cache miss - translate using Google Translation API
        log.debug("Translating recipe ID {} to locale {}", spoonacularId, locale);
        try {
            RecipeTranslationCache translationCache = translateAndCache(dto, spoonacularId, locale);
            applyCachedTranslation(dto, translationCache);
            enrichIngredientNames(dto, locale);
            return dto;
        } catch (GoogleTranslationException e) {
            log.error("Failed to translate recipe ID {} to locale {}: {}", 
                    spoonacularId, locale, e.getMessage());
            // Fall back to English content
            enrichIngredientNames(dto, locale);
            return dto;
        }
    }
    
    /**
     * Translate recipe content and cache the result.
     */
    private RecipeTranslationCache translateAndCache(RecipeDto dto, Integer spoonacularId, String locale) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(1); // 1 hour TTL
        
        // Translate name
        String translatedName = dto.getName() != null ? 
                translationClient.translateText(dto.getName(), locale) : null;
        
        // Translate description
        String translatedDescription = dto.getDescription() != null ? 
                translationClient.translateText(dto.getDescription(), locale) : null;
        
        // Translate instructions
        String translatedInstructions = dto.getInstructions() != null ? 
                translationClient.translateText(dto.getInstructions(), locale) : null;
        
        // Translate ingredient names (batch)
        List<String> translatedIngredientNames = new ArrayList<>();
        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            List<String> ingredientNames = dto.getIngredients().stream()
                    .map(IngredientDto::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (!ingredientNames.isEmpty()) {
                translatedIngredientNames = translationClient.translateTexts(ingredientNames, locale);
            }
        }
        
        // Store translated ingredients as JSON
        String translatedIngredientsJson = null;
        try {
            translatedIngredientsJson = objectMapper.writeValueAsString(translatedIngredientNames);
        } catch (Exception e) {
            log.warn("Failed to serialize translated ingredients to JSON", e);
        }
        
        // Create and save translation cache
        RecipeTranslationCache cache = new RecipeTranslationCache();
        cache.setSpoonacularId(spoonacularId);
        cache.setLocale(locale);
        cache.setTranslatedName(translatedName);
        cache.setTranslatedDescription(translatedDescription);
        cache.setTranslatedInstructions(translatedInstructions);
        cache.setTranslatedIngredients(translatedIngredientsJson);
        cache.setCachedAt(now);
        cache.setExpiresAt(expiresAt);
        
        translationCacheRepository.save(cache);
        log.debug("Cached translation for recipe ID {} in locale {}", spoonacularId, locale);
        
        return cache;
    }
    
    /**
     * Apply cached translation to DTO.
     */
    private void applyCachedTranslation(RecipeDto dto, RecipeTranslationCache cache) {
        if (cache.getTranslatedName() != null) {
            dto.setName(cache.getTranslatedName());
        }
        if (cache.getTranslatedDescription() != null) {
            dto.setDescription(cache.getTranslatedDescription());
        }
        if (cache.getTranslatedInstructions() != null) {
            dto.setInstructions(cache.getTranslatedInstructions());
        }
        
        // Apply translated ingredient names if available
        if (cache.getTranslatedIngredients() != null && dto.getIngredients() != null) {
            try {
                List<String> translatedNames = objectMapper.readValue(
                        cache.getTranslatedIngredients(), 
                        new TypeReference<List<String>>() {});
                
                if (translatedNames.size() == dto.getIngredients().size()) {
                    for (int i = 0; i < dto.getIngredients().size(); i++) {
                        dto.getIngredients().get(i).setName(translatedNames.get(i));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to deserialize translated ingredients from cache", e);
            }
        }
    }
    
    /**
     * Enrich ingredient names in RecipeDto with translations.
     * Uses batch loading to avoid N+1 queries.
     * 
     * @param dto the recipe DTO containing ingredients
     * @param userLocale the user's locale
     */
    private void enrichIngredientNames(RecipeDto dto, String userLocale) {
        if (dto.getIngredients() == null || dto.getIngredients().isEmpty()) {
            return;
        }
        
        // Collect ingredient IDs
        List<Long> ingredientIds = dto.getIngredients().stream()
            .map(IngredientDto::getIngredientId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        
        if (ingredientIds.isEmpty()) {
            return;
        }
        
        // Batch load all ingredients
        Map<Long, Ingredient> ingredientsMap = ingredientRepository.findAllById(ingredientIds)
            .stream()
            .collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        
        // Apply translations
        for (IngredientDto ingredientDto : dto.getIngredients()) {
            if (ingredientDto.getIngredientId() != null) {
                Ingredient ingredient = ingredientsMap.get(ingredientDto.getIngredientId());
                if (ingredient != null) {
                    String translatedName = translationResolver.resolveIngredientName(ingredient, userLocale);
                    if (translatedName != null) {
                        ingredientDto.setName(translatedName);
                    }
                }
            }
        }
    }
    
    /**
     * Validate and normalize user locale.
     * Returns the validated locale code or defaults to English if invalid.
     * 
     * @param userLocale the user's locale string (can be null, empty, or blank)
     * @return validated locale code, or "en" as default
     */
    private String validateAndNormalizeLocale(String userLocale) {
        return SupportedLocale.fromCode(userLocale)
            .map(SupportedLocale::getCode)
            .orElse(SupportedLocale.EN.getCode());
    }
}
