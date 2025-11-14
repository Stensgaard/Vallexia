-- Migration: Create recipe management tables
-- Version: V3
-- Description: Create complete recipe schema including recipes, ingredients, nutritional info,
--              recipe ingredients, tags, and favorites tables with proper relationships and indexes.

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
COMMENT ON COLUMN ingredients.name IS 'Unique ingredient name';

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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_prep_time CHECK (prep_time_minutes IS NULL OR prep_time_minutes >= 0),
    CONSTRAINT chk_cook_time CHECK (cook_time_minutes IS NULL OR cook_time_minutes >= 0),
    CONSTRAINT chk_total_time CHECK (total_time_minutes IS NULL OR total_time_minutes >= 0),
    CONSTRAINT chk_servings CHECK (servings >= 1),
    CONSTRAINT chk_difficulty_level CHECK (difficulty_level IS NULL OR difficulty_level IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_category CHECK (category IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK', 'DESSERT', 'APPETIZER', 'BEVERAGE', 'OTHER')),
    CONSTRAINT chk_cuisine_type CHECK (cuisine_type IS NULL OR cuisine_type IN (
        'AMERICAN', 'ITALIAN', 'MEXICAN', 'CHINESE', 'JAPANESE', 'THAI', 'INDIAN',
        'FRENCH', 'MEDITERRANEAN', 'GREEK', 'SPANISH', 'GERMAN', 'BRITISH',
        'KOREAN', 'VIETNAMESE', 'MIDDLE_EASTERN', 'CARIBBEAN', 'AFRICAN',
        'SOUTH_AMERICAN', 'FUSION'
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

-- ============================================================================
-- RECIPE INGREDIENTS TABLE
-- ============================================================================
-- Join table storing ingredients for each recipe with quantities and units.
-- ============================================================================
CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(50),
    notes VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE RESTRICT,
    CONSTRAINT chk_quantity CHECK (quantity > 0)
);

COMMENT ON TABLE recipe_ingredients IS 'Ingredients used in recipes with quantities and units';
COMMENT ON COLUMN recipe_ingredients.recipe_id IS 'Recipe this ingredient belongs to';
COMMENT ON COLUMN recipe_ingredients.ingredient_id IS 'Ingredient reference';
COMMENT ON COLUMN recipe_ingredients.quantity IS 'Quantity of ingredient needed';
COMMENT ON COLUMN recipe_ingredients.unit IS 'Unit of measurement (e.g., cup, tablespoon, gram)';
COMMENT ON COLUMN recipe_ingredients.notes IS 'Optional notes about this ingredient in the recipe';
COMMENT ON COLUMN recipe_ingredients.display_order IS 'Order in which ingredient appears in recipe';

-- ============================================================================
-- NUTRITIONAL INFO TABLE
-- ============================================================================
-- Stores nutritional information for recipes including macros and micronutrients.
-- ============================================================================
CREATE TABLE nutritional_info (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL UNIQUE,
    calories DECIMAL(10, 2) NOT NULL,
    protein DECIMAL(10, 2),
    carbs DECIMAL(10, 2),
    fats DECIMAL(10, 2),
    fiber DECIMAL(10, 2),
    sodium DECIMAL(10, 2),
    sugar DECIMAL(10, 2),
    per_serving BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT chk_calories CHECK (calories > 0),
    CONSTRAINT chk_protein CHECK (protein IS NULL OR protein >= 0),
    CONSTRAINT chk_carbs CHECK (carbs IS NULL OR carbs >= 0),
    CONSTRAINT chk_fats CHECK (fats IS NULL OR fats >= 0),
    CONSTRAINT chk_fiber CHECK (fiber IS NULL OR fiber >= 0),
    CONSTRAINT chk_sodium CHECK (sodium IS NULL OR sodium >= 0),
    CONSTRAINT chk_sugar CHECK (sugar IS NULL OR sugar >= 0)
);

COMMENT ON TABLE nutritional_info IS 'Nutritional information for recipes';
COMMENT ON COLUMN nutritional_info.recipe_id IS 'Recipe this nutritional info belongs to (unique)';
COMMENT ON COLUMN nutritional_info.calories IS 'Total calories';
COMMENT ON COLUMN nutritional_info.protein IS 'Protein in grams';
COMMENT ON COLUMN nutritional_info.carbs IS 'Carbohydrates in grams';
COMMENT ON COLUMN nutritional_info.fats IS 'Fats in grams';
COMMENT ON COLUMN nutritional_info.fiber IS 'Fiber in grams';
COMMENT ON COLUMN nutritional_info.sodium IS 'Sodium in milligrams';
COMMENT ON COLUMN nutritional_info.sugar IS 'Sugar in grams';
COMMENT ON COLUMN nutritional_info.per_serving IS 'True if values are per serving, false if total';

-- ============================================================================
-- RECIPE TAGS TABLE (ElementCollection)
-- ============================================================================
-- Stores flexible tags/categories for recipes to enable advanced search.
-- ============================================================================
CREATE TABLE recipe_tags (
    recipe_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (recipe_id, tag),
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

COMMENT ON TABLE recipe_tags IS 'Flexible tags for recipe categorization (ElementCollection mapping)';
COMMENT ON COLUMN recipe_tags.tag IS 'Tag name (lowercase, trimmed)';

-- ============================================================================
-- FAVORITE RECIPES TABLE
-- ============================================================================
-- Stores user favorite recipes with unique constraint on user-recipe pair.
-- ============================================================================
CREATE TABLE favorite_recipes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_recipe UNIQUE (user_id, recipe_id)
);

COMMENT ON TABLE favorite_recipes IS 'User favorite recipes';
COMMENT ON COLUMN favorite_recipes.user_id IS 'User who favorited the recipe';
COMMENT ON COLUMN favorite_recipes.recipe_id IS 'Recipe that was favorited';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
-- Create indexes to optimize common query patterns for recipes.
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

-- Recipe ingredients lookup
CREATE INDEX idx_recipe_ingredients_recipe_id ON recipe_ingredients(recipe_id);
CREATE INDEX idx_recipe_ingredients_ingredient_id ON recipe_ingredients(ingredient_id);

-- Ingredient name lookup (case-insensitive search)
CREATE INDEX idx_ingredients_name_lower ON ingredients(lower(name));

-- Favorites lookup
CREATE INDEX idx_favorite_recipes_user_id ON favorite_recipes(user_id);
CREATE INDEX idx_favorite_recipes_recipe_id ON favorite_recipes(recipe_id);

-- Nutritional info lookup
CREATE INDEX idx_nutritional_info_recipe_id ON nutritional_info(recipe_id);
