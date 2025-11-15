-- Migration: Create nutritional_goals table
-- Version: V4
-- Description: Create nutritional_goals table to store user daily nutritional targets
--              including macros and micros. This depends on the users table.

-- ============================================================================
-- NUTRITIONAL GOALS TABLE
-- ============================================================================
-- Stores user daily nutritional targets including macros and micros.
-- ============================================================================
CREATE TABLE nutritional_goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    daily_calories DECIMAL(10,2) NOT NULL,
    daily_protein DECIMAL(10,2),
    daily_carbs DECIMAL(10,2),
    daily_fats DECIMAL(10,2),
    protein_percentage DECIMAL(5,2),
    carbs_percentage DECIMAL(5,2),
    fats_percentage DECIMAL(5,2),
    daily_fiber DECIMAL(5,2),
    daily_sodium DECIMAL(10,2),
    daily_sugar DECIMAL(10,2),
    goal_type VARCHAR(50) NOT NULL DEFAULT 'MAINTENANCE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE nutritional_goals IS 'User daily nutritional targets and goals';
COMMENT ON COLUMN nutritional_goals.daily_calories IS 'Daily target calories';
COMMENT ON COLUMN nutritional_goals.daily_protein IS 'Daily target protein in grams (optional)';
COMMENT ON COLUMN nutritional_goals.daily_carbs IS 'Daily target carbohydrates in grams (optional, max 1500g)';
COMMENT ON COLUMN nutritional_goals.daily_fats IS 'Daily target fats in grams (optional)';
COMMENT ON COLUMN nutritional_goals.protein_percentage IS 'Target protein percentage of total calories (0-100)';
COMMENT ON COLUMN nutritional_goals.carbs_percentage IS 'Target carbs percentage of total calories (0-100)';
COMMENT ON COLUMN nutritional_goals.fats_percentage IS 'Target fats percentage of total calories (0-100)';
COMMENT ON COLUMN nutritional_goals.daily_fiber IS 'Daily target fiber in grams';
COMMENT ON COLUMN nutritional_goals.daily_sodium IS 'Daily target sodium in milligrams';
COMMENT ON COLUMN nutritional_goals.daily_sugar IS 'Daily target sugar in grams';
COMMENT ON COLUMN nutritional_goals.goal_type IS 'Nutritional goal type: MAINTENANCE, WEIGHT_LOSS, WEIGHT_GAIN, etc.';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_nutritional_goals_user_id ON nutritional_goals(user_id);
