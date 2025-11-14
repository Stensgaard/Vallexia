-- Migration: Create default admin user
-- Version: V5 (Dev migration)
-- Description: Creates a default admin user for development and testing purposes.
--              Admin user has both USER and ADMIN roles.
--              Password: AdminPass123! (BCrypt hash with strength 12)
--
-- NOTE: If the admin user already exists, this migration will be skipped (ON CONFLICT).
--       To reset the admin password, delete the user first or manually update the password_hash.
--
-- Password hash generation:
--   The password hash is generated for "AdminPass123!" using BCrypt with strength 12.
--   To generate a new hash, use:
--     BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
--     String hash = encoder.encode("AdminPass123!");

-- Insert admin user (skip if already exists)
INSERT INTO users (
    username,
    email,
    password_hash,
    enabled,
    account_non_expired,
    account_non_locked,
    credentials_non_expired,
    failed_login_attempts,
    household_size,
    subscription_status,
    created_at,
    updated_at
)
VALUES (
    'admin',
    'admin@example.com',
    -- BCrypt hash for "AdminPass123!" (strength 12)
    -- Generated using: new BCryptPasswordEncoder(12).encode("AdminPass123!")
    '$2a$12$FQtLtoF.5DKWt18mEjLTF.ahwJjNR.bxxN3iJEHPzbB8NE0AQFmH6',
    true,
    true,
    true,
    true,
    0,
    1,
    'FREE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;

-- Add USER role to admin (skip if already exists)
INSERT INTO user_roles (user_id, role)
SELECT id, 'USER'
FROM users
WHERE username = 'admin'
ON CONFLICT (user_id, role) DO NOTHING;

-- Add ADMIN role to admin (skip if already exists)
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE username = 'admin'
ON CONFLICT (user_id, role) DO NOTHING;

-- Add default meal types to admin user (skip if already exists)
INSERT INTO user_meal_types (user_id, meal_type)
SELECT id, unnest(ARRAY['BREAKFAST', 'LUNCH', 'DINNER']::VARCHAR[])
FROM users
WHERE username = 'admin'
ON CONFLICT (user_id, meal_type) DO NOTHING;
