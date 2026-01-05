package com.vallexia.recipe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for cleaning up expired recipe cache entries.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Slf4j
@Component
public class RecipeCacheCleanupJob {
    
    private final RecipeCacheService cacheService;
    
    public RecipeCacheCleanupJob(RecipeCacheService cacheService) {
        this.cacheService = cacheService;
    }
    
    /**
     * Clean up expired cache entries and translations every hour.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredEntries() {
        log.debug("Running recipe cache cleanup job");
        try {
            // Cleanup expired recipes (this also deletes associated translations via cascade)
            int deletedRecipes = cacheService.cleanupExpiredEntries();
            
            // Also cleanup any orphaned expired translations (safety net)
            int deletedTranslations = cacheService.cleanupExpiredTranslations();
            
            log.info("Recipe cache cleanup completed. Deleted {} expired recipes and {} expired translations", 
                    deletedRecipes, deletedTranslations);
        } catch (Exception e) {
            log.error("Error during recipe cache cleanup", e);
        }
    }
}
