-- Migration: Create audit_logs table
-- Version: V5
-- Description: Create audit_logs table for storing security and application events
--              for auditing purposes. This table is immutable - records cannot be
--              updated or deleted once created. Additional indexes and immutability
--              enforcement are added in V6.

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

COMMENT ON TABLE audit_logs IS 'Audit log table for security event tracking. Updates are prevented by database triggers. Deletes are allowed for retention policy management via scheduled job.';
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
-- BASIC INDEXES FOR PERFORMANCE
-- ============================================================================
-- Additional indexes and immutability enforcement are added in V6
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_success ON audit_logs(success);
