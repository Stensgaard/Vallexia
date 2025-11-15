-- Migration: Create recipe_tags and favorite_recipes tables
-- Version: V11
-- Description: Create recipe_tags table for flexible tags/categories and
--              favorite_recipes table for user favorite recipes. This depends
--              on both recipes and users tables.

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
CREATE INDEX idx_favorite_recipes_user_id ON favorite_recipes(user_id);
CREATE INDEX idx_favorite_recipes_recipe_id ON favorite_recipes(recipe_id);
