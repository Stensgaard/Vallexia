-- Database initialization script
-- This script runs when the PostgreSQL container starts for the first time

-- Create the main database if it doesn't exist
-- (This is handled by POSTGRES_DB environment variable)

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create any additional schemas or configurations
-- Add any initial data or configurations here

-- Grant necessary permissions (restricted to application needs)
-- Use current database name dynamically
DO $$
DECLARE
    current_db_name TEXT;
BEGIN
    -- Get current database name
    SELECT current_database() INTO current_db_name;
    
    -- Grant permissions on current database
    EXECUTE format('GRANT CONNECT ON DATABASE %I TO vallexia_user', current_db_name);
END $$;

-- Grant schema permissions
GRANT USAGE ON SCHEMA public TO vallexia_user;
GRANT CREATE ON SCHEMA public TO vallexia_user;

-- Grant table permissions (will apply to future tables)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO vallexia_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO vallexia_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO vallexia_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO vallexia_user;
