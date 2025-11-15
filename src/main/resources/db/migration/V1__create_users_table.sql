-- Migration: Create users table and user_roles
-- Version: V1
-- Description: Create core user authentication tables including users and user_roles
--              for authorization. This is the foundation for all user-related features.

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
-- INDEXES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_subscription_status ON users(subscription_status);
CREATE INDEX idx_users_subscription_expires ON users(subscription_expires_at);
