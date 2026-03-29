-- 为 tb_achievement 表添加 file_id 字段，用于关联成就图片
-- 论文/专利成就可直接关联图片文件，无需通过 tb_introduce_image 表

ALTER TABLE tb_achievement
ADD COLUMN IF NOT EXISTS file_id BIGINT;

COMMENT ON COLUMN tb_achievement.file_id IS '关联文件ID，用于论文/专利成就的图片';

-- 添加索引用于查询优化（非外键约束）
CREATE INDEX IF NOT EXISTS idx_achievement_file_id ON tb_achievement(file_id);
