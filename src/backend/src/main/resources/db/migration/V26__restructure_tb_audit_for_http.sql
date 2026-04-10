-- Restructure tb_audit table for HTTP-level API audit logging

-- Add new columns
ALTER TABLE tb_audit ADD COLUMN request_method VARCHAR(10);
ALTER TABLE tb_audit ADD COLUMN request_uri VARCHAR(500);
ALTER TABLE tb_audit ADD COLUMN http_status INTEGER;
ALTER TABLE tb_audit ADD COLUMN response_message VARCHAR(500);
ALTER TABLE tb_audit ADD COLUMN stack_trace TEXT;
ALTER TABLE tb_audit ADD COLUMN duration_ms BIGINT;

-- Clear existing data (table has no production data)
DELETE FROM tb_audit;

-- Drop old columns
ALTER TABLE tb_audit DROP COLUMN action;
ALTER TABLE tb_audit DROP COLUMN remarks;

-- Add new indexes
CREATE INDEX idx_audit_request_uri ON tb_audit(request_uri);
CREATE INDEX idx_audit_http_status ON tb_audit(http_status);
