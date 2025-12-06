-- Migration: Create user_meal_types table
-- Version: V2
-- Description: Create user_meal_types table to store meal types that each user wants to track.
--              This is a user preference table that depends on the users table.

-- ============================================================================
-- USER MEAL TYPES TABLE (ElementCollection)
-- ============================================================================
-- Stores meal types that each user wants to track.
-- ============================================================================
CREATE TABLE user_meal_types (
    user_id BIGINT NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, meal_type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE user_meal_types IS 'Meal types that users want to track (ElementCollection mapping)';
COMMENT ON COLUMN user_meal_types.meal_type IS 'Meal type: BREAKFAST, LUNCH, DINNER, or SNACK';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_user_meal_types_user_id ON user_meal_types(user_id);
