-- Migration: Create base tables for user management, preferences, goals, and audit logs
-- Version: V1
-- Description: Create complete base schema including users, dietary preferences, nutritional goals, and audit logs.
--              This migration consolidates the initial schema with household and subscription fields.

-- ============================================================================
-- USERS TABLE
-- ============================================================================
-- Stores user account information including authentication details, profile data,
-- household settings, and subscription information.
-- ============================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    account_locked_until TIMESTAMP,
    -- Household and meal planning fields
    household_size INTEGER NOT NULL DEFAULT 1,
    -- Subscription management fields
    subscription_status VARCHAR(50) NOT NULL DEFAULT 'FREE',
    subscription_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Constraints
    CONSTRAINT chk_household_size CHECK (household_size >= 1 AND household_size <= 20),
    CONSTRAINT chk_subscription_status CHECK (subscription_status IN ('FREE', 'PREMIUM', 'FAMILY', 'CANCELLED', 'EXPIRED'))
);

COMMENT ON TABLE users IS 'User accounts with authentication, profile, household, and subscription information';
COMMENT ON COLUMN users.username IS 'Unique username (3-20 characters)';
COMMENT ON COLUMN users.email IS 'Unique email address';
COMMENT ON COLUMN users.password_hash IS 'Hashed password using BCrypt';
COMMENT ON COLUMN users.enabled IS 'Whether the user account is enabled';
COMMENT ON COLUMN users.account_non_locked IS 'Whether the account is locked due to failed login attempts';
COMMENT ON COLUMN users.failed_login_attempts IS 'Number of consecutive failed login attempts';
COMMENT ON COLUMN users.account_locked_until IS 'Timestamp until which the account is locked (null if not locked)';
COMMENT ON COLUMN users.household_size IS 'Number of people in the household (1-20)';
COMMENT ON COLUMN users.subscription_status IS 'Subscription status: FREE, PREMIUM, FAMILY, CANCELLED, or EXPIRED';
COMMENT ON COLUMN users.subscription_expires_at IS 'Timestamp when subscription expires (null for FREE tier)';

-- ============================================================================
-- USER ROLES TABLE (ElementCollection)
-- ============================================================================
-- Stores user roles for authorization. Each user can have multiple roles.
-- ============================================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE user_roles IS 'User roles for authorization (ElementCollection mapping)';
COMMENT ON COLUMN user_roles.role IS 'Role name (enum value)';

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



-- TODO: move this to v2? together with the other audit log indexes?
-- ============================================================================
-- AUDIT LOGS TABLE
-- ============================================================================
-- Stores security and application events for auditing purposes.
-- This table is immutable - records cannot be updated or deleted once created.
-- ============================================================================
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    event_description VARCHAR(500) NOT NULL,
    user_id BIGINT,
    username VARCHAR(255),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    request_method VARCHAR(10),
    request_uri VARCHAR(500),
    response_status INTEGER,
    details TEXT,
    success BOOLEAN,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE audit_logs IS 'Immutable audit log table for security event tracking. Updates and deletes are prevented by database triggers.';
COMMENT ON COLUMN audit_logs.event_type IS 'Type of security/application event (enum value)';
COMMENT ON COLUMN audit_logs.event_description IS 'Human-readable description of the event';
COMMENT ON COLUMN audit_logs.user_id IS 'ID of the user who triggered the event (null if anonymous)';
COMMENT ON COLUMN audit_logs.username IS 'Username of the user who triggered the event (null if anonymous)';
COMMENT ON COLUMN audit_logs.ip_address IS 'Client IP address extracted from request with proxy validation';
COMMENT ON COLUMN audit_logs.user_agent IS 'User agent string from the HTTP request';
COMMENT ON COLUMN audit_logs.request_method IS 'HTTP method (GET, POST, PUT, DELETE, etc.)';
COMMENT ON COLUMN audit_logs.request_uri IS 'Request URI path';
COMMENT ON COLUMN audit_logs.response_status IS 'HTTP response status code';
COMMENT ON COLUMN audit_logs.details IS 'Additional event details in JSON or text format';
COMMENT ON COLUMN audit_logs.success IS 'Whether the operation was successful';
COMMENT ON COLUMN audit_logs.timestamp IS 'Timestamp when the event was logged (immutable)';

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- Users table indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_subscription_status ON users(subscription_status);
CREATE INDEX idx_users_subscription_expires ON users(subscription_expires_at);

-- User meal types indexes
CREATE INDEX idx_user_meal_types_user_id ON user_meal_types(user_id);

-- Dietary preferences indexes
CREATE INDEX idx_dietary_preferences_user_id ON dietary_preferences(user_id);

-- Nutritional goals indexes
CREATE INDEX idx_nutritional_goals_user_id ON nutritional_goals(user_id);

-- Audit logs indexes (additional indexes added in V2 migration)
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_success ON audit_logs(success);

