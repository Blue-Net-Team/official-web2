-- 允许 grade 为 null，表示该考核不限年级（用于全局考核场景）
ALTER TABLE tb_assessment_time ALTER COLUMN grade DROP NOT NULL;
