ALTER TABLE tb_verify_code ADD COLUMN scene VARCHAR(50) NOT NULL DEFAULT 'login';

CREATE INDEX idx_verify_code_scene ON tb_verify_code(scene);
CREATE INDEX idx_verify_code_target_scene ON tb_verify_code(target, scene);
