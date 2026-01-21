package com.vallexia.recipe.integration.client;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.vallexia.config.api.GoogleTranslationProperties;
import com.vallexia.recipe.integration.exception.GoogleTranslationException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client for interacting with Google Cloud Translation API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Slf4j
@Component
public class GoogleTranslationClient {
    
    private final Translate translate;
    private final GoogleTranslationProperties properties;
    
    public GoogleTranslationClient(GoogleTranslationProperties properties) {
        this.properties = properties;
        
        if (!properties.isEnabled()) {
            log.warn("Google Translation is disabled. Translations will not be performed.");
            this.translate = null;
            return;
        }
        
        try {
            TranslateOptions.Builder builder = TranslateOptions.newBuilder();
            
            if (properties.getProjectId() != null && !properties.getProjectId().isBlank()) {
                builder.setProjectId(properties.getProjectId());
            }
            
            this.translate = builder.build().getService();
            log.info("Google Translation client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Google Translation client", e);
            throw new GoogleTranslationException("Failed to initialize Google Translation client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Translate a single text to target language.
     * 
     * @param text the text to translate
     * @param targetLanguage the target language code (e.g., "da", "en")
     * @return translated text
     * @throws GoogleTranslationException if translation fails
     */
    public String translateText(String text, String targetLanguage) {
        if (!properties.isEnabled() || translate == null) {
            log.debug("Translation disabled, returning original text");
            return text;
        }
        
        if (text == null || text.isBlank()) {
            return text;
        }
        
        if (targetLanguage.equals(properties.getDefaultSourceLanguage())) {
            return text;
        }
        
        try {
            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(properties.getDefaultSourceLanguage()),
                    Translate.TranslateOption.targetLanguage(targetLanguage)
            );
            
            String translated = translation.getTranslatedText();
            log.debug("Translated text from {} to {}: {} -> {}", 
                    properties.getDefaultSourceLanguage(), targetLanguage, 
                    text.substring(0, Math.min(50, text.length())), 
                    translated.substring(0, Math.min(50, translated.length())));
            
            return translated;
        } catch (Exception e) {
            log.error("Failed to translate text to {}: {}", targetLanguage, e.getMessage());
            throw new GoogleTranslationException("Failed to translate text: " + e.getMessage(), e);
        }
    }

    /**
     * Translate a single text from an explicit source language to target language.
     *
     * <p>This is useful when the source is not the application's default source language
     * (e.g., translating Danish flyer text to English).</p>
     *
     * @param text the text to translate
     * @param sourceLanguage the source language code (e.g., "da", "en")
     * @param targetLanguage the target language code (e.g., "da", "en")
     * @return translated text
     */
    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        if (!properties.isEnabled() || translate == null) {
            log.debug("Translation disabled, returning original text");
            return text;
        }

        if (text == null || text.isBlank()) {
            return text;
        }

        if (targetLanguage == null || targetLanguage.isBlank()) {
            return text;
        }

        if (sourceLanguage != null && !sourceLanguage.isBlank()
            && targetLanguage.equals(sourceLanguage)) {
            return text;
        }

        try {
            Translation translation;
            if (sourceLanguage == null || sourceLanguage.isBlank()) {
                translation = translate.translate(
                    text,
                    Translate.TranslateOption.targetLanguage(targetLanguage)
                );
            } else {
                translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(sourceLanguage),
                    Translate.TranslateOption.targetLanguage(targetLanguage)
                );
            }

            return translation.getTranslatedText();
        } catch (Exception e) {
            log.error("Failed to translate text from {} to {}: {}", sourceLanguage, targetLanguage, e.getMessage());
            throw new GoogleTranslationException("Failed to translate text: " + e.getMessage(), e);
        }
    }
    
    /**
     * Translate multiple texts in batch to target language.
     * 
     * @param texts list of texts to translate
     * @param targetLanguage the target language code (e.g., "da", "en")
     * @return list of translated texts in same order
     * @throws GoogleTranslationException if translation fails
     */
    public List<String> translateTexts(List<String> texts, String targetLanguage) {
        if (!properties.isEnabled() || translate == null) {
            log.debug("Translation disabled, returning original texts");
            return texts;
        }
        
        if (texts == null || texts.isEmpty()) {
            return texts != null ? texts : new ArrayList<>();
        }
        
        if (targetLanguage.equals(properties.getDefaultSourceLanguage())) {
            return texts;
        }
        
        try {
            List<Translation> translations = translate.translate(
                    texts,
                    Translate.TranslateOption.sourceLanguage(properties.getDefaultSourceLanguage()),
                    Translate.TranslateOption.targetLanguage(targetLanguage)
            );
            
            List<String> translated = translations.stream()
                    .map(Translation::getTranslatedText)
                    .collect(Collectors.toList());
            
            log.debug("Translated {} texts from {} to {}", 
                    texts.size(), properties.getDefaultSourceLanguage(), targetLanguage);
            
            return translated;
        } catch (Exception e) {
            log.error("Failed to translate texts to {}: {}", targetLanguage, e.getMessage());
            throw new GoogleTranslationException("Failed to translate texts: " + e.getMessage(), e);
        }
    }
}
