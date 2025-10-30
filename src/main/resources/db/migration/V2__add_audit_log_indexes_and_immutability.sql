-- Migration: Add audit log indexes and immutability enforcement
-- Version: V2
-- Description: Add additional performance indexes for audit logs and enforce immutability
--              through database triggers to maintain audit trail integrity.

-- Add additional indexes for commonly queried fields
CREATE INDEX IF NOT EXISTS idx_audit_logs_username ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_logs_ip_address ON audit_logs(ip_address);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id_timestamp ON audit_logs(user_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_event_type_timestamp ON audit_logs(event_type, timestamp DESC);

-- Create a function to prevent updates to audit logs
CREATE OR REPLACE FUNCTION prevent_audit_log_update()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit logs are immutable and cannot be updated';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to prevent updates
DROP TRIGGER IF EXISTS audit_log_immutability_trigger ON audit_logs;
CREATE TRIGGER audit_log_immutability_trigger
    BEFORE UPDATE ON audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION prevent_audit_log_update();

-- Update comment to document immutability (if not already present)
COMMENT ON TABLE audit_logs IS 'Immutable audit log table for security event tracking. Updates are prevented by trigger.';

-- Add comments on important columns (if not already present)
COMMENT ON COLUMN audit_logs.event_type IS 'Type of security event (enum)';
COMMENT ON COLUMN audit_logs.ip_address IS 'Client IP address extracted from request with proxy validation';
COMMENT ON COLUMN audit_logs.timestamp IS 'Timestamp when the event was logged (immutable)';


