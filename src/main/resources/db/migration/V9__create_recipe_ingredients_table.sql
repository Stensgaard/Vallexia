-- Migration: Create recipe_ingredients table
-- Version: V9
-- Description: Create recipe_ingredients table as a join table storing ingredients
--              for each recipe with quantities and units. This depends on both
--              recipes and ingredients tables.

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
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_recipe_ingredients_recipe_id ON recipe_ingredients(recipe_id);
CREATE INDEX idx_recipe_ingredients_ingredient_id ON recipe_ingredients(ingredient_id);
