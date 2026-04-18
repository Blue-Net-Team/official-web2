ALTER TABLE tb_enroll ADD COLUMN IF NOT EXISTS gender VARCHAR(20) DEFAULT 'unknown';

COMMENT ON COLUMN tb_enroll.gender IS '性别：male/female/unknown';
