-- Migration: Create recipe_translation_cache table
-- Version: V16
-- Description: Create recipe_translation_cache table for caching translated recipe content
--              with 1-hour TTL. Translations are linked to recipe_cache by spoonacular_id.

-- ============================================================================
-- RECIPE TRANSLATION CACHE TABLE
-- ============================================================================
-- Stores translated recipe content (name, description, instructions, ingredients)
-- for different locales. Translations expire after 1 hour (same as recipe cache).
-- ============================================================================
CREATE TABLE recipe_translation_cache (
    spoonacular_id INTEGER NOT NULL,
    locale VARCHAR(10) NOT NULL,
    translated_name VARCHAR(255),
    translated_description TEXT,
    translated_instructions TEXT,
    translated_ingredients JSONB,
    cached_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    PRIMARY KEY (spoonacular_id, locale),
    FOREIGN KEY (spoonacular_id) REFERENCES recipe_cache(spoonacular_id) ON DELETE CASCADE
);

COMMENT ON TABLE recipe_translation_cache IS 'Cached translations of Spoonacular recipes with 1-hour TTL';
COMMENT ON COLUMN recipe_translation_cache.spoonacular_id IS 'Spoonacular recipe ID (part of composite key)';
COMMENT ON COLUMN recipe_translation_cache.locale IS 'Target locale for translation (part of composite key)';
COMMENT ON COLUMN recipe_translation_cache.translated_name IS 'Translated recipe name';
COMMENT ON COLUMN recipe_translation_cache.translated_description IS 'Translated recipe description';
COMMENT ON COLUMN recipe_translation_cache.translated_instructions IS 'Translated recipe instructions';
COMMENT ON COLUMN recipe_translation_cache.translated_ingredients IS 'JSON array of translated ingredient names';
COMMENT ON COLUMN recipe_translation_cache.cached_at IS 'Timestamp when translation was cached';
COMMENT ON COLUMN recipe_translation_cache.expires_at IS 'Timestamp when translation expires (cached_at + 1 hour)';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
-- Index for cleanup job (finding expired entries)
CREATE INDEX idx_recipe_translation_cache_expires_at ON recipe_translation_cache(expires_at);

-- Index for efficient lookup by recipe and locale
CREATE INDEX idx_recipe_translation_cache_spoonacular_locale ON recipe_translation_cache(spoonacular_id, locale);





