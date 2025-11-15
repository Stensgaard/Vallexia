-- Migration: Create nutritional_info table
-- Version: V10
-- Description: Create nutritional_info table for storing nutritional information
--              for recipes including macros and micronutrients. This depends on
--              the recipes table.

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
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_nutritional_info_recipe_id ON nutritional_info(recipe_id);
