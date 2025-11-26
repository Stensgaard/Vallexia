-- Migration: Create user settings table
-- Version: V14
-- Description: Create user_settings table for storing user display preferences including
--              localization, date formats, measurement units, and other UI preferences.
--              This depends on the users table.

-- ============================================================================
-- USER SETTINGS TABLE
-- ============================================================================
-- Stores user display preferences including language, country, date formats,
-- timezone, first day of week, measurement system, number formatting, and currency.
-- ============================================================================
CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    country VARCHAR(2),
    date_format VARCHAR(20) NOT NULL DEFAULT 'MM_DD_YYYY',
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    first_day_of_week VARCHAR(10) NOT NULL DEFAULT 'MONDAY',
    measurement_system VARCHAR(10) NOT NULL DEFAULT 'METRIC',
    number_decimal_separator VARCHAR(1) NOT NULL DEFAULT '.',
    number_thousands_separator VARCHAR(1) NOT NULL DEFAULT ',',
    currency VARCHAR(3),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_first_day_of_week CHECK (first_day_of_week IN ('SUNDAY', 'MONDAY')),
    CONSTRAINT chk_measurement_system CHECK (measurement_system IN ('METRIC', 'IMPERIAL')),
    CONSTRAINT chk_date_format CHECK (date_format IN ('MM_DD_YYYY', 'DD_MM_YYYY', 'YYYY_MM_DD', 'DD_MM_YYYY_DOT'))
);

COMMENT ON TABLE user_settings IS 'User display preferences including localization, date formats, and measurement units';
COMMENT ON COLUMN user_settings.user_id IS 'Foreign key to users table (OneToOne relationship)';
COMMENT ON COLUMN user_settings.language IS 'Interface language code (ISO 639-1, default: en)';
COMMENT ON COLUMN user_settings.country IS 'Country code (ISO 3166-1 alpha-2)';
COMMENT ON COLUMN user_settings.date_format IS 'Date format preference: MM/DD/YYYY, DD/MM/YYYY, YYYY-MM-DD, or DD.MM.YYYY';
COMMENT ON COLUMN user_settings.timezone IS 'IANA timezone identifier (e.g., America/New_York, Europe/London)';
COMMENT ON COLUMN user_settings.first_day_of_week IS 'First day of week: SUNDAY or MONDAY';
COMMENT ON COLUMN user_settings.measurement_system IS 'Measurement system: METRIC or IMPERIAL';
COMMENT ON COLUMN user_settings.number_decimal_separator IS 'Decimal separator for numbers (default: .)';
COMMENT ON COLUMN user_settings.number_thousands_separator IS 'Thousands separator for numbers (default: ,)';
COMMENT ON COLUMN user_settings.currency IS 'Currency code (ISO 4217, nullable for future shopping list features)';

-- Note: Index on user_id is automatically created by the UNIQUE constraint above
