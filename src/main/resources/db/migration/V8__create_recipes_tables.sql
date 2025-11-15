-- Migration: Create recipes and recipe_translations tables
-- Version: V8
-- Description: Create recipes table and recipe_translations table for storing
--              recipe information including name, description, instructions, timing,
--              difficulty, category, cuisine type, and visibility settings.
--              This depends on the users table.

-- ============================================================================
-- RECIPES TABLE
-- ============================================================================
-- Stores recipe information including name, description, instructions, timing,
-- difficulty, category, cuisine type, and visibility settings.
-- ============================================================================
CREATE TABLE recipes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    instructions TEXT NOT NULL,
    prep_time_minutes INTEGER,
    cook_time_minutes INTEGER,
    total_time_minutes INTEGER,
    servings INTEGER NOT NULL DEFAULT 1,
    difficulty_level VARCHAR(20),
    category VARCHAR(50) NOT NULL,
    cuisine_type VARCHAR(50),
    image_url VARCHAR(500),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    base_locale VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_prep_time CHECK (prep_time_minutes IS NULL OR prep_time_minutes >= 0),
    CONSTRAINT chk_cook_time CHECK (cook_time_minutes IS NULL OR cook_time_minutes >= 0),
    CONSTRAINT chk_total_time CHECK (total_time_minutes IS NULL OR total_time_minutes >= 0),
    CONSTRAINT chk_servings CHECK (servings >= 1),
    CONSTRAINT chk_difficulty_level CHECK (difficulty_level IS NULL OR difficulty_level IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_category CHECK (category IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK', 'DESSERT', 'APPETIZER', 'BEVERAGE')),
    CONSTRAINT chk_cuisine_type CHECK (cuisine_type IS NULL OR cuisine_type IN (
        'AMERICAN', 'ITALIAN', 'MEXICAN', 'CHINESE', 'JAPANESE', 'THAI', 'INDIAN',
        'FRENCH', 'MEDITERRANEAN', 'GREEK', 'SPANISH', 'GERMAN', 'BRITISH',
        'KOREAN', 'VIETNAMESE', 'MIDDLE_EASTERN', 'CARIBBEAN', 'AFRICAN',
        'SOUTH_AMERICAN'
    ))
);

COMMENT ON TABLE recipes IS 'Recipes with ingredients, instructions, and metadata';
COMMENT ON COLUMN recipes.user_id IS 'User who created the recipe';
COMMENT ON COLUMN recipes.name IS 'Recipe name';
COMMENT ON COLUMN recipes.description IS 'Brief description of the recipe';
COMMENT ON COLUMN recipes.instructions IS 'Detailed cooking instructions';
COMMENT ON COLUMN recipes.prep_time_minutes IS 'Preparation time in minutes';
COMMENT ON COLUMN recipes.cook_time_minutes IS 'Cooking time in minutes';
COMMENT ON COLUMN recipes.total_time_minutes IS 'Total time (prep + cook) in minutes';
COMMENT ON COLUMN recipes.servings IS 'Base number of servings';
COMMENT ON COLUMN recipes.difficulty_level IS 'Difficulty level: EASY, MEDIUM, HARD, or EXPERT';
COMMENT ON COLUMN recipes.category IS 'Recipe category';
COMMENT ON COLUMN recipes.cuisine_type IS 'Cuisine type';
COMMENT ON COLUMN recipes.image_url IS 'URL to recipe image';
COMMENT ON COLUMN recipes.is_public IS 'Whether recipe is publicly visible';
COMMENT ON COLUMN recipes.base_locale IS 'Base locale/language the recipe was originally created in (en, da)';

-- ============================================================================
-- RECIPE TRANSLATIONS TABLE
-- ============================================================================
-- Stores translations of recipe content (name, description, instructions) for different locales.
-- ============================================================================
CREATE TABLE recipe_translations (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    locale VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    instructions TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT uk_recipe_locale UNIQUE (recipe_id, locale)
    -- Note: Locale validation is handled at the application level via SupportedLocale enum
    -- This allows for easier addition of new locales without database migration changes
);

COMMENT ON TABLE recipe_translations IS 'Translations of recipe content for different locales';
COMMENT ON COLUMN recipe_translations.recipe_id IS 'Recipe this translation belongs to';
COMMENT ON COLUMN recipe_translations.locale IS 'Locale of this translation (must be one of the supported locales defined in SupportedLocale enum)';
COMMENT ON COLUMN recipe_translations.name IS 'Translated recipe name';
COMMENT ON COLUMN recipe_translations.description IS 'Translated recipe description';
COMMENT ON COLUMN recipe_translations.instructions IS 'Translated recipe instructions';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
-- Full-text search on recipe name
CREATE INDEX idx_recipes_name ON recipes USING gin(to_tsvector('english', name));

-- Search by creator
CREATE INDEX idx_recipes_user_id ON recipes(user_id);

-- Search by category
CREATE INDEX idx_recipes_category ON recipes(category);

-- Search by cuisine type
CREATE INDEX idx_recipes_cuisine_type ON recipes(cuisine_type);

-- Search by difficulty level
CREATE INDEX idx_recipes_difficulty_level ON recipes(difficulty_level);

-- Search by public visibility
CREATE INDEX idx_recipes_is_public ON recipes(is_public) WHERE is_public = TRUE;

-- Search by created date (for recent recipes)
CREATE INDEX idx_recipes_created_at ON recipes(created_at DESC);

-- Index for efficient lookup by recipe and locale
CREATE INDEX idx_recipe_translations_recipe_locale ON recipe_translations(recipe_id, locale);
