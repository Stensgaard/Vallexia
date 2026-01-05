-- Migration: Create favorite_recipes table
-- Version: V11
-- Description: Create favorite_recipes table for user favorite recipes using Spoonacular IDs.
--              This depends on the users table.

-- ============================================================================
-- FAVORITE RECIPES TABLE
-- ============================================================================
-- Stores user favorite recipes with unique constraint on user-spoonacular_id pair.
-- Recipes are identified by Spoonacular API IDs (external recipes).
-- ============================================================================
CREATE TABLE favorite_recipes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    spoonacular_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_spoonacular_id UNIQUE (user_id, spoonacular_id)
);

COMMENT ON TABLE favorite_recipes IS 'User favorite recipes from Spoonacular API';
COMMENT ON COLUMN favorite_recipes.user_id IS 'User who favorited the recipe';
COMMENT ON COLUMN favorite_recipes.spoonacular_id IS 'Spoonacular recipe ID';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_favorite_recipes_user_id ON favorite_recipes(user_id);
CREATE INDEX idx_favorite_recipes_spoonacular_id ON favorite_recipes(spoonacular_id);
