-- Migration: Create dietary preferences tables
-- Version: V3
-- Description: Create dietary_preferences table and related ElementCollection tables
--              for storing user dietary restrictions, allergies, and cuisine preferences.
--              This depends on the users table.

-- ============================================================================
-- DIETARY PREFERENCES TABLE
-- ============================================================================
-- Stores user dietary preferences including restrictions, allergies, cuisine
-- preferences, and disliked ingredients.
-- ============================================================================
CREATE TABLE dietary_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE dietary_preferences IS 'User dietary preferences and restrictions';

-- ============================================================================
-- DIETARY RESTRICTIONS TABLE (ElementCollection)
-- ============================================================================
-- Stores individual dietary restrictions associated with dietary preferences.
-- ============================================================================
CREATE TABLE dietary_restrictions (
    preferences_id BIGINT NOT NULL,
    restriction VARCHAR(100) NOT NULL,
    PRIMARY KEY (preferences_id, restriction),
    FOREIGN KEY (preferences_id) REFERENCES dietary_preferences(id) ON DELETE CASCADE
);

COMMENT ON TABLE dietary_restrictions IS 'Dietary restrictions (ElementCollection mapping)';
COMMENT ON COLUMN dietary_restrictions.restriction IS 'Dietary restriction type (enum value)';

-- ============================================================================
-- ALLERGIES TABLE (ElementCollection)
-- ============================================================================
-- Stores user allergies associated with dietary preferences.
-- ============================================================================
CREATE TABLE allergies (
    preferences_id BIGINT NOT NULL,
    allergy VARCHAR(100) NOT NULL,
    PRIMARY KEY (preferences_id, allergy),
    FOREIGN KEY (preferences_id) REFERENCES dietary_preferences(id) ON DELETE CASCADE
);

COMMENT ON TABLE allergies IS 'User allergies (ElementCollection mapping)';
COMMENT ON COLUMN allergies.allergy IS 'Allergy type (enum value)';

-- ============================================================================
-- CUISINE PREFERENCES TABLE (ElementCollection)
-- ============================================================================
-- Stores preferred cuisine types associated with dietary preferences.
-- ============================================================================
CREATE TABLE cuisine_preferences (
    preferences_id BIGINT NOT NULL,
    cuisine VARCHAR(100) NOT NULL,
    PRIMARY KEY (preferences_id, cuisine),
    FOREIGN KEY (preferences_id) REFERENCES dietary_preferences(id) ON DELETE CASCADE
);

COMMENT ON TABLE cuisine_preferences IS 'Preferred cuisine types (ElementCollection mapping)';
COMMENT ON COLUMN cuisine_preferences.cuisine IS 'Cuisine type (enum value)';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_dietary_preferences_user_id ON dietary_preferences(user_id);
