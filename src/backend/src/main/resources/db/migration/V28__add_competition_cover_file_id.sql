-- 为竞赛表添加封面文件ID字段
ALTER TABLE tb_competition ADD COLUMN IF NOT EXISTS cover_file_id BIGINT;

COMMENT ON COLUMN tb_competition.cover_file_id IS '封面文件ID';
