-- Migration: Create recipe_cache table
-- Version: V15
-- Description: Create recipe_cache table for caching Spoonacular recipes with 1-hour TTL.
--              Includes searchable fields and indexes for efficient querying.

-- ============================================================================
-- RECIPE CACHE TABLE
-- ============================================================================
-- Stores cached Spoonacular recipes with denormalized searchable fields.
-- Recipes are cached for 1 hour per Spoonacular Terms of Use.
-- ============================================================================
CREATE TABLE recipe_cache (
    spoonacular_id INTEGER PRIMARY KEY,
    search_hash VARCHAR(64),  -- Hash of search parameters for search result caching
    recipe_data JSONB NOT NULL,  -- Full recipe JSON from Spoonacular
    recipe_name VARCHAR(255),  -- Denormalized for searchability
    cuisine VARCHAR(50)[],  -- Array of cuisines for filtering
    diets VARCHAR(50)[],  -- Array of diet types for filtering
    intolerances VARCHAR(50)[],  -- Array of intolerances for filtering
    ingredients VARCHAR(255)[],  -- Array of ingredient names for search
    cached_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

COMMENT ON TABLE recipe_cache IS 'Cached Spoonacular recipes with 1-hour TTL';
COMMENT ON COLUMN recipe_cache.spoonacular_id IS 'Spoonacular recipe ID (primary key)';
COMMENT ON COLUMN recipe_cache.search_hash IS 'Hash of search parameters for caching search results';
COMMENT ON COLUMN recipe_cache.recipe_data IS 'Full recipe JSON from Spoonacular API';
COMMENT ON COLUMN recipe_cache.recipe_name IS 'Recipe name for text search';
COMMENT ON COLUMN recipe_cache.cuisine IS 'Array of cuisines for filtering';
COMMENT ON COLUMN recipe_cache.diets IS 'Array of diet types for filtering';
COMMENT ON COLUMN recipe_cache.intolerances IS 'Array of intolerances for filtering';
COMMENT ON COLUMN recipe_cache.ingredients IS 'Array of ingredient names for search';
COMMENT ON COLUMN recipe_cache.cached_at IS 'Timestamp when recipe was cached';
COMMENT ON COLUMN recipe_cache.expires_at IS 'Timestamp when cache entry expires (cached_at + 1 hour)';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
-- Index for search result lookups
CREATE INDEX idx_recipe_cache_search_hash ON recipe_cache(search_hash);

-- Index for cleanup job (finding expired entries)
CREATE INDEX idx_recipe_cache_expires_at ON recipe_cache(expires_at);

-- GIN index for full-text search on recipe name
CREATE INDEX idx_recipe_cache_name ON recipe_cache USING gin(to_tsvector('english', recipe_name));

-- GIN indexes for array filtering
CREATE INDEX idx_recipe_cache_cuisine ON recipe_cache USING gin(cuisine);
CREATE INDEX idx_recipe_cache_diets ON recipe_cache USING gin(diets);
CREATE INDEX idx_recipe_cache_intolerances ON recipe_cache USING gin(intolerances);
CREATE INDEX idx_recipe_cache_ingredients ON recipe_cache USING gin(ingredients);