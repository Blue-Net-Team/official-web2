-- Add request_uri_pattern column for API statistics aggregation

-- Add column storing normalized URI pattern (e.g., /api/v1/file/download/{fileId})
ALTER TABLE tb_audit ADD COLUMN request_uri_pattern VARCHAR(500);

-- Add index for GROUP BY aggregation queries
CREATE INDEX idx_audit_uri_pattern ON tb_audit(request_uri_pattern);
