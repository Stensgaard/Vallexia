-- Migration: Create ingredient nutrition table
-- Version: V4
-- Description: Add ingredient_nutrition table to store nutritional data per ingredient
--              for automatic recipe nutrition calculation.

-- ============================================================================
-- INGREDIENT_NUTRITION TABLE
-- ============================================================================
-- Stores nutritional information for ingredients (per 100g standard).
-- Enables automatic calculation of recipe nutrition from ingredient quantities.
-- ============================================================================
CREATE TABLE ingredient_nutrition (
    id BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL UNIQUE,
    calories_per_100g DECIMAL(10, 2),
    protein_per_100g DECIMAL(10, 2),
    carbs_per_100g DECIMAL(10, 2),
    fats_per_100g DECIMAL(10, 2),
    fiber_per_100g DECIMAL(10, 2),
    sodium_per_100g DECIMAL(10, 2),
    sugar_per_100g DECIMAL(10, 2),
    standard_unit VARCHAR(50) DEFAULT 'g',
    conversion_factor_to_grams DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE,
    CONSTRAINT chk_calories CHECK (calories_per_100g IS NULL OR calories_per_100g >= 0),
    CONSTRAINT chk_protein CHECK (protein_per_100g IS NULL OR protein_per_100g >= 0),
    CONSTRAINT chk_carbs CHECK (carbs_per_100g IS NULL OR carbs_per_100g >= 0),
    CONSTRAINT chk_fats CHECK (fats_per_100g IS NULL OR fats_per_100g >= 0),
    CONSTRAINT chk_fiber CHECK (fiber_per_100g IS NULL OR fiber_per_100g >= 0),
    CONSTRAINT chk_sodium CHECK (sodium_per_100g IS NULL OR sodium_per_100g >= 0),
    CONSTRAINT chk_sugar CHECK (sugar_per_100g IS NULL OR sugar_per_100g >= 0),
    CONSTRAINT chk_conversion_factor CHECK (conversion_factor_to_grams IS NULL OR conversion_factor_to_grams > 0)
);

CREATE INDEX idx_ingredient_nutrition_ingredient_id ON ingredient_nutrition(ingredient_id);

COMMENT ON TABLE ingredient_nutrition IS 'Nutritional information for ingredients (per 100g standard)';
COMMENT ON COLUMN ingredient_nutrition.ingredient_id IS 'Reference to ingredient';
COMMENT ON COLUMN ingredient_nutrition.calories_per_100g IS 'Calories per 100g of ingredient';
COMMENT ON COLUMN ingredient_nutrition.protein_per_100g IS 'Protein (g) per 100g of ingredient';
COMMENT ON COLUMN ingredient_nutrition.carbs_per_100g IS 'Carbohydrates (g) per 100g of ingredient';
COMMENT ON COLUMN ingredient_nutrition.fats_per_100g IS 'Fats (g) per 100g of ingredient';
COMMENT ON COLUMN ingredient_nutrition.fiber_per_100g IS 'Fiber (g) per 100g of ingredient';
COMMENT ON COLUMN ingredient_nutrition.sodium_per_100g IS 'Sodium (mg) per 100g of ingredient';
COMMENT ON COLUMN ingredient_nutrition.sugar_per_100g IS 'Sugar (g) per 100g of ingredient';
COMMENT ON COLUMN ingredient_nutrition.standard_unit IS 'Standard unit for this ingredient (e.g., g, ml, cup)';
COMMENT ON COLUMN ingredient_nutrition.conversion_factor_to_grams IS 'Conversion factor from standard unit to grams';
