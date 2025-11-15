-- Migration: Create ingredients and ingredient_translations tables
-- Version: V7
-- Description: Create ingredients table and ingredient_translations table for storing
--              unique food ingredients and their translations for different locales.
--              This is the foundation for recipe management.

-- ============================================================================
-- INGREDIENTS TABLE
-- ============================================================================
-- Stores unique food ingredients that can be used across multiple recipes.
-- ============================================================================
CREATE TABLE ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ingredients IS 'Unique food ingredients available for use in recipes';
COMMENT ON COLUMN ingredients.name IS 'Unique ingredient name (base locale)';

-- ============================================================================
-- INGREDIENT TRANSLATIONS TABLE
-- ============================================================================
-- Stores translations of ingredient names for different locales.
-- ============================================================================
CREATE TABLE ingredient_translations (
    id BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL,
    locale VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE,
    CONSTRAINT uk_ingredient_locale UNIQUE (ingredient_id, locale)
);

COMMENT ON TABLE ingredient_translations IS 'Translations of ingredient names for different locales';
COMMENT ON COLUMN ingredient_translations.ingredient_id IS 'Ingredient this translation belongs to';
COMMENT ON COLUMN ingredient_translations.locale IS 'Locale of this translation (must be one of the supported locales defined in SupportedLocale enum)';
COMMENT ON COLUMN ingredient_translations.name IS 'Translated ingredient name';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_ingredient_translations_ingredient_locale ON ingredient_translations(ingredient_id, locale);
CREATE INDEX idx_ingredients_name_lower ON ingredients(lower(name));
